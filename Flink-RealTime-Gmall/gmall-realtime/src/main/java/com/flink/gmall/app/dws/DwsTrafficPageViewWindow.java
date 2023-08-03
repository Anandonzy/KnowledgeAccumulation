package com.flink.gmall.app.dws;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.flink.gmall.bean.TrafficHomeDetailPageViewBean;
import com.flink.gmall.utils.DateFormatUtil;
import com.flink.gmall.utils.MyClickHouseUtil;
import com.flink.gmall.utils.MyKafkaUtil;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.time.Duration;

/**
 * @Author wangziyu1
 * @Date 2022/8/30 11:14
 * @Version 1.0
 *  (1) 读取页面主题数据，封装为流
 *
 * （2）统计页面浏览时长、页面浏览数、会话数，转换数据结构
 *
 * 创建实体类，将独立访客数、跳出会话数置为 0，将页面浏览数置为 1（只要有一条页面浏览日志，则页面浏览数加一），获取日志中的页面浏览时长，赋值给实体类的同名字段，最后判断 last_page_id 是否为 null，如果是，说明页面是首页，开启了一个新的会话，将会话数置为 1，否则置为 0。补充维度字段，窗口起始和结束时间置为空字符串。下游要根据水位线开窗，所以要补充事件时间字段，此处将日志生成时间 ts 作为事件时间字段即可。最后将实体类对象发往下游。
 *
 * （3）读取用户跳出明细数据
 *
 * （4）转换用户跳出流数据结构
 *
 * 封装实体类，维度字段和时间戳处理与页面流相同，跳出数置为1，其余度量字段置为 0。将数据发往下游。
 *
 * （5）读取独立访客明细数据
 *
 * （6）转换独立访客流数据结构
 *
 * 处理过程与跳出流同理。
 *
 * （7）union 合并三条流
 *
 * （8）设置水位线；
 *
 * （9）按照维度字段分组；
 *
 * （10）开窗
 *
 *   跳出行为判定的超时时间为10s，假设某条日志属于跳出数据，如果它对应的事件时间为15s，要判定是否跳出需要在水位线达到25s 时才能做到，若窗口大小为 10s，这条数据应进入 10~20s窗口，但是拿到这条数据时水位线已达到 25s，所属窗口已被销毁。这样就导致跳出会话数永远为 0，显然是有问题的。要避免这种情况，必须设置窗口延迟关闭，延迟关闭时间大于等于跳出判定的超时时间才能保证跳出数据不会被漏掉。但是这样会严重影响时效性，如果企业要求延迟时间设置为半小时，那么窗口就要延迟半小时关闭。要统计跳出行为相关的指标，就必须接受它对时效性带来的负面影响。
 *
 * （11）聚合计算
 *
 * 度量字段求和，每个窗口数据聚合完毕之后补充窗口起始时间和结束时间字段。
 *
 * 在 ClickHouse 中，ts 将作为版本字段用于去重，ReplacingMergeTree 会在分区合并时对比排序字段相同数据的 ts，保留 ts 最大的数据。此处将时间戳字段置为当前系统时间，这样可以保证数据重复计算时保留的是最后一次计算的结果。
 *
 * （12）将数据写入 ClickHouse。
 */
//数据流：web/app -> Nginx -> 日志服务器(.log) -> Flume -> Kafka(ODS) -> FlinkApp -> Kafka(DWD) -> FlinkApp -> ClickHouse(DWS)
//程  序：     Mock(lg.sh) -> Flume(f1) -> Kafka(ZK) -> BaseLogApp -> Kafka(ZK) -> DwsTrafficPageViewWindow -> ClickHouse(ZK)
public class DwsTrafficPageViewWindow {

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
//        System.setProperty("HADOOP_USER_NAME", "wangizyu1");

        //TODO 2.读取 Kafka 页面日志主题数据创建流
        String topic = "dwd_traffic_page_log_new";
        String groupId = "dws_traffic_page_view_window_test";
        DataStreamSource<String> kafkaDS = env.addSource(MyKafkaUtil.getFlinkKafkaConsumer(topic, groupId));

        //TODO 3.将每行数据转换为JSON对象并过滤(首页和商品详情页)
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaDS.flatMap(new FlatMapFunction<String, JSONObject>() {
            @Override
            public void flatMap(String value, Collector<JSONObject> out) throws Exception {

                //转换json对象
                JSONObject jsonObject = JSON.parseObject(value);

                //获取当前页面id
                String pageId = jsonObject.getJSONObject("page").getString("page_id");

                if ("home".equals(pageId) || "good_detail".equals(pageId)) {
                    out.collect(jsonObject);
                }
            }
        });

        //TODO 4.提取事件时间生成Watermark
        SingleOutputStreamOperator<JSONObject> jsonObjWithWmDS = jsonObjDS.assignTimestampsAndWatermarks(
                WatermarkStrategy.<JSONObject>forBoundedOutOfOrderness(Duration.ofSeconds(2)).withTimestampAssigner(new SerializableTimestampAssigner<JSONObject>() {
                    @Override
                    public long extractTimestamp(JSONObject jsonObject, long l) {
                        return jsonObject.getLong("ts");
                    }
                }));

        //TODO 5.按照Mid分组
        KeyedStream<JSONObject, String> keyedStream = jsonObjWithWmDS.keyBy(jsonObject -> jsonObject.getJSONObject("common").getString("mid"));

        //TODO 6.使用状态编程过滤出首页与商品详情页的独立访客
        SingleOutputStreamOperator<TrafficHomeDetailPageViewBean> trafficHomeDetailDS = keyedStream.flatMap(new RichFlatMapFunction<JSONObject, TrafficHomeDetailPageViewBean>() {

            private ValueState<String> homeLastState;
            private ValueState<String> detailLastState;

            @Override
            public void open(Configuration parameters) throws Exception {

                //设置状态描述器ttl
                StateTtlConfig ttlConfig = new StateTtlConfig.Builder(Time.days(1))
                        .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite) //创建或者被修改重置ttl
                        .build();
                //获取状态描述器
                ValueStateDescriptor<String> homeStateDes = new ValueStateDescriptor<>("home-state", String.class);
                ValueStateDescriptor<String> detailStateDes = new ValueStateDescriptor<>("detail-state", String.class);

                //开启ttl
                homeStateDes.enableTimeToLive(ttlConfig);
                detailStateDes.enableTimeToLive(ttlConfig);

                homeLastState = getRuntimeContext().getState(homeStateDes);
                detailLastState = getRuntimeContext().getState(detailStateDes);
            }

            @Override
            public void flatMap(JSONObject value, Collector<TrafficHomeDetailPageViewBean> collector) throws Exception {
                Long ts = value.getLong("ts");
                String curDt = DateFormatUtil.toDate(ts);
                String homeLastDt = homeLastState.value();
                String detailLastDt = detailLastState.value();

                long homeCt = 0;
                long detailCt = 0;

                //如果状态为空或者状态时间与当前时间不同,则为需要的数据
                if ("home".equals(value.getJSONObject("page").getString("page_id"))) {
                    if (homeLastDt == null || !homeLastDt.equals(curDt)) { //为空或者 不等于当前时间 则统计
                        homeCt = 1L;
                        homeLastState.update(curDt);
                    }
                } else {
                    if (detailLastDt == null || !detailLastDt.equals(curDt)) {
                        detailCt = 1L;
                        detailLastState.update(curDt);
                    }
                }

                //满足任意一个数据不等于0,则就可以写出
                if (homeCt == 1L || detailCt == 1L) {
                    collector.collect(new TrafficHomeDetailPageViewBean("", "",
                            homeCt, detailCt, ts));
                }

            }
        });

        //TODO 7.开窗聚合
        SingleOutputStreamOperator<TrafficHomeDetailPageViewBean> reduce = trafficHomeDetailDS.windowAll(TumblingEventTimeWindows.of(org.apache.flink.streaming.api.windowing.time.Time.seconds(10))).reduce(new ReduceFunction<TrafficHomeDetailPageViewBean>() {
            @Override
            public TrafficHomeDetailPageViewBean reduce(TrafficHomeDetailPageViewBean value1, TrafficHomeDetailPageViewBean value2) throws Exception {
                value1.setGoodDetailUvCt(value1.getGoodDetailUvCt() + value2.getGoodDetailUvCt());
                value1.setHomeUvCt(value1.getHomeUvCt() + value2.getHomeUvCt());
                return value1;
            }
        }, new AllWindowFunction<TrafficHomeDetailPageViewBean, TrafficHomeDetailPageViewBean, TimeWindow>() {
            @Override
            public void apply(TimeWindow timeWindow, Iterable<TrafficHomeDetailPageViewBean> value, Collector<TrafficHomeDetailPageViewBean> out) throws Exception {

                //获取数据
                TrafficHomeDetailPageViewBean pageViewBean = value.iterator().next();
                pageViewBean.setStt(DateFormatUtil.toYmdHms(timeWindow.getStart()));
                pageViewBean.setEdt(DateFormatUtil.toYmdHms(timeWindow.getEnd()));

                //补充信息
                pageViewBean.setTs(System.currentTimeMillis());

                //输出数据
                out.collect(pageViewBean);
            }
        });

        reduce.print(">>>>");
        reduce.addSink(MyClickHouseUtil.getSinkFunction("insert into dws_traffic_page_view_window values(?,?,?,?,?)"));

        env.execute("DwsTrafficPageViewWindow");
    }
}
