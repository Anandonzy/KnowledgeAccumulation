package com.flink.gmall.app.dws;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.flink.gmall.bean.CartAddUuBean;
import com.flink.gmall.utils.DateFormatUtil;
import com.flink.gmall.utils.MyClickHouseUtil;
import com.flink.gmall.utils.MyKafkaUtil;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
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
 * @Date 2022/8/30 16:33
 * @Version 1.0
 * 用户域用户注册各窗口汇总表
 * 1）从 Kafka 加购明细主题读取数据
 * 2）转换数据结构
 * 将流中数据由 String 转换为 JSONObject。
 * 3）设置水位线
 * 4）按照用户 id 分组
 * 5）过滤独立用户加购记录
 * 运用 Flink 状态编程，将用户末次加购日期维护到状态中。
 * 如果末次登陆日期为 null 或者不等于当天日期，则保留数据并更新状态，否则丢弃，不做操作。
 * 6）开窗、聚合
 * 统计窗口中数据条数即为加购独立用户数，补充窗口起始时间、关闭时间，将时间戳字段置为当前系统时间，发送到下游。
 * 7）将数据写入 ClickHouse。
 */
//数据流：Web/app -> nginx -> 业务服务器(Mysql) -> Maxwell -> Kafka(ODS) -> FlinkApp -> Kafka(DWD) -> FlinkApp -> ClickHouse(DWS)
//程  序：Mock  ->  Mysql  ->  Maxwell -> Kafka(ZK)  ->  DwdTradeCartAdd -> Kafka(ZK) -> DwsTradeCartAddUuWindow -> ClickHouse(ZK)
public class DwsTradeCartAddUuWindow {


    public static void main(String[] args) throws Exception {

        //TODO 1. 获取执行环境

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
        //TODO 2. 读取kafka DWD层 加购事实表
        String topic = "dwd_trade_cart_add";
        String groupId = "dws_trade_cart_add_uu_window_test";
        DataStreamSource<String> kafkaDS = env.addSource(MyKafkaUtil.getFlinkKafkaConsumer(topic, groupId));

        //TODO 3. 将数据转换为jsonobject
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaDS.map(JSON::parseObject);

        //TODO 4.提取事件时间生成watermark
        SingleOutputStreamOperator<JSONObject> jsonObjWmDS = jsonObjDS.assignTimestampsAndWatermarks(WatermarkStrategy.<JSONObject>forBoundedOutOfOrderness(Duration.ofSeconds(2)).withTimestampAssigner(new SerializableTimestampAssigner<JSONObject>() {
            @Override
            public long extractTimestamp(JSONObject element, long l) {
                String operateTime = element.getString("operate_time");

                if (operateTime != null) {
                    return DateFormatUtil.toTs(operateTime, true);
                } else {
                    return DateFormatUtil.toTs(element.getString("create_time"), true);
                }
            }
        }));

        //TODO 5. 按照user_id分组
        KeyedStream<JSONObject, String> keyedStream = jsonObjWmDS.keyBy(json -> json.getString("user_id"));

        //TODO 6.使用状态编程提取独立用户
        SingleOutputStreamOperator<CartAddUuBean> cartAddDS = keyedStream.flatMap(new RichFlatMapFunction<JSONObject, CartAddUuBean>() {
            private ValueState<String> lastCarAddState;

            @Override
            public void open(Configuration parameters) throws Exception {
                //设置状态ttl为1天
                StateTtlConfig ttlConfig = new StateTtlConfig.Builder(Time.days(1))
                        .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite).build();

                ValueStateDescriptor<String> laststateDescriptor = new ValueStateDescriptor<>("last-cart", String.class);
                laststateDescriptor.enableTimeToLive(ttlConfig);

                lastCarAddState = getRuntimeContext().getState(laststateDescriptor);
            }

            @Override
            public void flatMap(JSONObject value, Collector<CartAddUuBean> out) throws Exception {

                //获取状态数据 以及当前数据的日期
                String lastDt = lastCarAddState.value();
                String operateTime = value.getString("operate_time");
                String curDt = null;

                if (operateTime != null) {
                    curDt = operateTime.split(" ")[0];
                } else {
                    String createTime = value.getString("create_time");
                    curDt = createTime.split(" ")[0];
                }
                if (lastDt == null || !lastDt.equals(curDt)) {
                    lastCarAddState.update(curDt);
                    out.collect(new CartAddUuBean("", "", 1L, null));
                }
            }
        });


        //TODO 7.开窗 聚合
        SingleOutputStreamOperator<CartAddUuBean> resultDS = cartAddDS.windowAll(TumblingEventTimeWindows.of(org.apache.flink.streaming.api.windowing.time.Time.seconds(10))).reduce(new ReduceFunction<CartAddUuBean>() {
            @Override
            public CartAddUuBean reduce(CartAddUuBean value1, CartAddUuBean value2) throws Exception {
                value1.setCartAddUuCt(value1.getCartAddUuCt() + value2.getCartAddUuCt());
                return value1;
            }
        }, new AllWindowFunction<CartAddUuBean, CartAddUuBean, TimeWindow>() {
            @Override
            public void apply(TimeWindow timeWindow, Iterable<CartAddUuBean> iterable, Collector<CartAddUuBean> out) throws Exception {
                //获取数据
                CartAddUuBean next = iterable.iterator().next();

                //补充信息
                next.setStt(DateFormatUtil.toYmdHms(timeWindow.getStart()));
                next.setEdt(DateFormatUtil.toYmdHms(timeWindow.getEnd()));
                next.setTs(System.currentTimeMillis());

                //输出数据
                out.collect(next);
            }
        });

        //TODO 8.将数据写出到ClickHouse
        resultDS.print(">>>>>>>>>>>");
        resultDS.addSink(MyClickHouseUtil.getSinkFunction("insert into dws_trade_cart_add_uu_window values (?,?,?,?)"));

        //TODO 9.启动任务
        env.execute("DwsTradeCartAddUuWindow");
    }
}
