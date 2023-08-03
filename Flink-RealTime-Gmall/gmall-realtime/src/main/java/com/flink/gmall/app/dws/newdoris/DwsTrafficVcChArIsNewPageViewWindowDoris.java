package com.flink.gmall.app.dws.newdoris;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.flink.gmall.bean.TrafficPageViewBean;
import com.flink.gmall.utils.*;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.time.Duration;

/**
 * @Author wangziyu1
 * @Date 2022/9/19 19:14
 * @Version 1.0
 */

//数据流：web/app -> Nginx -> 日志服务器(.log) -> Flume -> Kafka(ODS) -> FlinkApp -> Kafka(DWD)
//数据流：web/app -> Nginx -> 日志服务器(.log) -> Flume -> Kafka(ODS) -> FlinkApp -> Kafka(DWD) -> FlinkApp -> Kafka(DWD)
//数据流：web/app -> Nginx -> 日志服务器(.log) -> Flume -> Kafka(ODS) -> FlinkApp -> Kafka(DWD) -> FlinkApp -> Kafka(DWD)
////////======> FlinkApp -> ClickHouse(DWS)

//程  序：     Mock(lg.sh) -> Flume(f1) -> Kafka(ZK) -> BaseLogApp -> Kafka(ZK)
//程  序：     Mock(lg.sh) -> Flume(f1) -> Kafka(ZK) -> BaseLogApp -> Kafka(ZK) -> DwdTrafficUserJumpDetail -> Kafka(ZK)
//程  序：     Mock(lg.sh) -> Flume(f1) -> Kafka(ZK) -> BaseLogApp -> Kafka(ZK) -> DwdTrafficUniqueVisitorDetail -> Kafka(ZK)
////////======> DwsTrafficVcChArIsNewPageViewWindow -> ClickHouse(ZK)
public class DwsTrafficVcChArIsNewPageViewWindowDoris {
    /**
     * vc、ch、ar、is_new  1、1、5623、0、0
     * vc、ch、ar、is_new  0、0、0、1、0
     * vc、ch、ar、is_new  0、0、0、0、1
     * <p>
     * vc、ch、ar、is_new  1、1、5623、1、1
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
        //TODO 1.获取执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        // 1.1 状态后端设置
//        env.enableCheckpointing(3000L, CheckpointingMode.EXACTLY_ONCE);
//        env.getCheckpointConfig().setCheckpointTimeout(60 * 1000L);
//        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(3000L);
//        env.getCheckpointConfig().enableExternalizedCheckpoints(
//                CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION
//        );
//        env.setRestartStrategy(RestartStrategies.failureRateRestart(
//                3, Time.days(1), Time.minutes(1)
//        ));
//        env.setStateBackend(new HashMapStateBackend());
//        env.getCheckpointConfig().setCheckpointStorage(
//                "hdfs://hadoop102:8020/ck"
//        );
//        System.setProperty("HADOOP_USER_NAME", "wangziyu1");

        //TODO 2.读取三个主题的数据创建流
        String uvTopic = "dwd_traffic_unique_visitor_detail";
        String ujdTopic = "dwd_traffic_user_jump_detail";
        String topic = "dwd_traffic_page_log_new";
        String groupId = "vccharisnew_pageview_window_test";
        DataStreamSource<String> uvDS = env.addSource(MyKafkaUtil.getFlinkKafkaConsumer(uvTopic, groupId));
        DataStreamSource<String> ujDS = env.addSource(MyKafkaUtil.getFlinkKafkaConsumer(ujdTopic, groupId));
        DataStreamSource<String> pageDS = env.addSource(MyKafkaUtil.getFlinkKafkaConsumer(topic, groupId));


        //TODO 3.统一数据格式
        SingleOutputStreamOperator<TrafficPageViewBean> trafficPageViewWithUvDS = uvDS.map(line -> {
            JSONObject jsonObject = JSON.parseObject(line);
            JSONObject common = jsonObject.getJSONObject("common");
            return new TrafficPageViewBean("", "",
                    common.getString("vc"),
                    common.getString("ch"),
                    common.getString("ar"),
                    common.getString("is_new"),DateFormatUtil.toDate(System.currentTimeMillis()),
                    1L, 0L, 0L, 0L, 0L,
                    jsonObject.getLong("ts"));
        });

        SingleOutputStreamOperator<TrafficPageViewBean> trafficPageViewWithUjDS = ujDS.map(line -> {
            JSONObject jsonObject = JSON.parseObject(line);
            JSONObject common = jsonObject.getJSONObject("common");

            return new TrafficPageViewBean("", "",
                    common.getString("vc"),
                    common.getString("ch"),
                    common.getString("ar"),
                    common.getString("is_new"),DateFormatUtil.toDate(System.currentTimeMillis()),
                    0L, 0L, 0L, 0L, 1L,
                    jsonObject.getLong("ts"));
        });

        SingleOutputStreamOperator<TrafficPageViewBean> trafficPageViewWithPageDS = pageDS.map(line -> {
            JSONObject jsonObject = JSON.parseObject(line);
            JSONObject common = jsonObject.getJSONObject("common");

            JSONObject page = jsonObject.getJSONObject("page");
            String lastPageId = page.getString("last_page_id");
            long sv = 0L;
            if (lastPageId == null) {
                sv = 1L;
            }

            return new TrafficPageViewBean("", "",
                    common.getString("vc"),
                    common.getString("ch"),
                    common.getString("ar"),
                    common.getString("is_new"),DateFormatUtil.toDate(System.currentTimeMillis()),
                    0L, sv, 1L, page.getLong("during_time"), 0L,
                    jsonObject.getLong("ts"));
        });

        //TODO 4.将三个数据流合并
        DataStream<TrafficPageViewBean> unionDS = trafficPageViewWithUvDS.union(trafficPageViewWithUjDS, trafficPageViewWithPageDS);

        //TODO 5.提取事件时间生成水位线waterMark
        SingleOutputStreamOperator<TrafficPageViewBean> trafficPageViewWithWmDS = unionDS.assignTimestampsAndWatermarks(WatermarkStrategy.<TrafficPageViewBean>forBoundedOutOfOrderness(Duration.ofSeconds(14)).withTimestampAssigner(new SerializableTimestampAssigner<TrafficPageViewBean>() {
            @Override
            public long extractTimestamp(TrafficPageViewBean trafficPageViewBean, long l) {
                return trafficPageViewBean.getTs();
            }
        }));

        //TODO 6. 分组 开窗 聚合
        WindowedStream<TrafficPageViewBean, Tuple4<String, String, String, String>, TimeWindow> windowedStream = trafficPageViewWithWmDS.keyBy(new KeySelector<TrafficPageViewBean, Tuple4<String, String, String, String>>() {
            @Override
            public Tuple4<String, String, String, String> getKey(TrafficPageViewBean value) throws Exception {

                return new Tuple4<>(value.getVc(),
                        value.getAr(),
                        value.getIsNew(),
                        value.getCh());
            }
        }).window(TumblingEventTimeWindows.of(Time.seconds(10)));//开窗10s

        //聚合函数
        /**
         * 增量聚合函数:
         *  来一条计算一条
         *  效率高、存储的数据量小
         *
         * 全量聚合函数:
         *   攒到一个集合中,最后统一计算
         *   可以求前百分比、可以获取窗口信息
         */

        //增量聚合
        //windowedStream.reduce(new ReduceFunction<TrafficPageViewBean>() {
        //    @Override
        //    public TrafficPageViewBean reduce(TrafficPageViewBean value1, TrafficPageViewBean value2) throws Exception {
        //        //这里面可以将value2的值累加到value1里面.
        //        return null;
        //    }
        //});
        //
        ////全量聚合
        //windowedStream.apply(new WindowFunction<TrafficPageViewBean, TrafficPageViewBean, Tuple4<String, String, String, String>, TimeWindow>() {
        //    @Override
        //    public void apply(Tuple4<String, String, String, String> stringStringStringStringTuple4, TimeWindow timeWindow, Iterable<TrafficPageViewBean> iterable, Collector<TrafficPageViewBean> collector) throws Exception {
        //    //这里面可以获取到状态信息
        //    }
        //});

        SingleOutputStreamOperator<TrafficPageViewBean> resultDS = windowedStream
                //.sideOutputLateData(new OutputTag<TrafficPageViewBean>("late") {}) //如果有迟到数据可以在这里测输出溜输出迟到数据 然后解决迟到数据.
                .reduce(new ReduceFunction<TrafficPageViewBean>() {
                    @Override
                    public TrafficPageViewBean reduce(TrafficPageViewBean value1, TrafficPageViewBean value2) throws Exception {
                        value1.setVc(value2.getVc());
                        value1.setUvCt(value1.getUvCt() + value2.getUvCt());
                        value1.setUjCt(value1.getUjCt() + value2.getUjCt());
                        value1.setPvCt(value1.getPvCt() + value2.getPvCt());
                        value1.setDurSum(value1.getDurSum() + value2.getDurSum());
                        return value1;
                    }
                }, new WindowFunction<TrafficPageViewBean, TrafficPageViewBean, Tuple4<String, String, String, String>, TimeWindow>() {
                    @Override
                    public void apply(Tuple4<String, String, String, String> key, TimeWindow window, Iterable<TrafficPageViewBean> input, Collector<TrafficPageViewBean> out) throws Exception {
                        //获取数据
                        TrafficPageViewBean next = input.iterator().next();
                        //补充信息
                        next.setStt(DateFormatUtil.toYmdHms(window.getStart()));
                        next.setEdt(DateFormatUtil.toYmdHms(window.getEnd()));

                        //修改ts
                        next.setTs(System.currentTimeMillis());

                        //输出数据
                        out.collect(next);
                    }
                });

        //TODO 7.将数据写出到ClickHouse
        //resultDS.print(">>>>");
        //resultDS.addSink(MyClickHouseUtil.getSinkFunction("insert into dws_traffic_vc_ch_ar_is_new_page_view_window values(?,?,?,?,?,?,?,?,?,?,?,?)"));

        //查看处理的结果
        //resultDS.map(bean -> {
        //    SerializeConfig config = new SerializeConfig();
        //    config.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase; //转成json的时候自动变成下划线
        //    String s = JSON.toJSONString(bean,config);
        //    System.out.println(s);
        //    return s;
        //});
        resultDS.map(bean -> {
                    SerializeConfig config = new SerializeConfig();
                    config.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase; //转成json的时候自动变成下划线
                    //System.out.println(bean.getVc());
                    String s = JSON.toJSONString(bean, config);
                    System.out.println(s);
                    return s;
                })
                .addSink(MyDorisSink.getDorisSink("dws_traffic_vc_ch_ar_is_new_page_view_window"));
        //使用bean的方式写入doris bean字段的顺序要和表的字段顺序一样 否则写入不进去. 而且jdbc的方式连接的端口是:9030
        //resultDS.addSink(new MyDorisSinkNew<TrafficPageViewBean>("dws_traffic_vc_ch_ar_is_new_page_view_window", "curDate"));

        //TODO 8.启动任务
        env.execute("DwsTrafficVcChArIsNewPageViewWindow");
    }
}