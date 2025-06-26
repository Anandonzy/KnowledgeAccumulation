package com.flink.gmall.app.dws;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.flink.gmall.bean.TradePaymentWindowBean;
import com.flink.gmall.utils.DateFormatUtil;
import com.flink.gmall.utils.MyClickHouseUtil;
import com.flink.gmall.utils.MyKafkaUtil;
import com.flink.gmall.utils.TimestampLtz3CompareUtil;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;

import java.time.Duration;
import java.util.Collection;

/**
 * @Author wangziyu1
 * @Date 2022/8/31 10:42
 * @Version 1.0
 * 交易域支付各窗口汇总表
 * <p>
 * 处理重复数据:
 * 使用的是方法一,定义一个状态,将数据存入状态,然后来一条和这个比较,留晚的那条放到状态;
 * 状态为null的时候,注册定时器,当定时器触发,则输出状态中的数据
 * <p>
 * （1）从 Kafka 支付成功明细主题读取数据
 * （2）转换数据结构
 * String 转换为 JSONObject。
 * （3）按照唯一键分组
 * （4）去重
 * 与前文同理。
 * （5）设置水位线，按照 user_id 分组
 * （6）统计独立支付人数和新增支付人数
 * 运用 Flink 状态编程，在状态中维护用户末次支付日期。
 * 若末次支付日期为 null，则将首次支付用户数和支付独立用户数均置为 1；否则首次支付用户数置为 0，判断末次支付日期是否为当日，如果不是当日则支付独立用户数置为 1，否则置为 0。最后将状态中的支付日期更新为当日。
 * （7）开窗、聚合
 * 度量字段求和，补充窗口起始时间和结束时间字段，ts 字段置为当前系统时间戳。
 * （8）写出到 ClickHouse
 */


//数据流：Web/app -> nginx -> 业务服务器(Mysql) -> Maxwell -> Kafka(ODS) -> FlinkApp -> Kafka(DWD) -> FlinkApp -> Kafka(DWD) -> FlinkApp -> Kafka(DWD) -> FlinkApp -> ClickHouse(DWS)
//程  序：Mock  ->  Mysql  ->  Maxwell -> Kafka(ZK)  ->  DwdTradeOrderPreProcess -> Kafka(ZK) -> DwdTradeOrderDetail -> Kafka(ZK) -> DwdTradePayDetailSuc -> Kafka(ZK) -> DwsTradePaymentSucWindow -> ClickHouse(ZK)
public class DwsTradePaymentSucWindow {

    /**
     * 针对于left join出现的重复数据问题:
     * 方案一:定义一个状态,将数据存入状态,然后来一条和这个比较,留晚的那条放到状态;状态为null的时候,注册定时器,当定时器触发,则输出状态中的数据
     * 方案二:开一个窗口,使用全量聚合函数,取出时间最大的一条数据进行输出
     * 使用增量聚合函数,保留时间最大的一条数据,进行输出
     * 方案三:如果后续需求没有用到left join右表的字段,那么则可以只保留第一条数据进行输出
     * 这里使用的是方案一
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {


        //TODO 1.获取执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1); //生产环境中设置为Kafka主题的分区数

        //1.1 开启CheckPoint
        //env.enableCheckpointing(5 * 60000L, CheckpointingMode.EXACTLY_ONCE);
        //env.getCheckpointConfig().setCheckpointTimeout(10 * 60000L);
        //env.getCheckpointConfig().setMaxConcurrentCheckpoints(2);
        //env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3, 5000L));

        //1.2 设置状态后端
        //env.setStateBackend(new HashMapStateBackend());
        //env.getCheckpointConfig().setCheckpointStorage("hdfs://hadoop102:8020/211126/ck");
        //System.setProperty("HADOOP_USER_NAME", "wangziyu1");

        //1.3 设置状态的TTL  生产环境设置为最大乱序程度
        //tableEnv.getConfig().setIdleStateRetention(Duration.ofSeconds(5));

        //TODO 2.读取DWD层成功支付主题数据创建流
        String topic = "dwd_trade_pay_detail_suc";
        String groupId = "dws_trade_payment_suc_window_test";
        DataStreamSource<String> kafkaDS = env.addSource(MyKafkaUtil.getFlinkKafkaConsumer(topic, groupId));

        //TODO 3.将数据转换为JSON对象
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaDS.flatMap(new FlatMapFunction<String, JSONObject>() {
            @Override
            public void flatMap(String value, Collector<JSONObject> out) throws Exception {
                try {
                    JSONObject jsonObject = JSON.parseObject(value);
                    out.collect(jsonObject);
                } catch (Exception e) {
                    System.out.println(">>>>>>>" + value);
                }

            }
        });

        //TODO 4.按照订单明细id分组
        KeyedStream<JSONObject, String> jsonObjKeyedByDetailIdDS = jsonObjDS.keyBy(json -> json.getString("order_detail_id"));

        /**
         * 如果要使用定时器 或者 测流输出 就只能用process函数处理
         */
        //TODO 5.使用状态编程保留最新的数据输出
        SingleOutputStreamOperator<JSONObject> processDS = jsonObjKeyedByDetailIdDS.process(new KeyedProcessFunction<String, JSONObject, JSONObject>() {

            private ValueState<JSONObject> valueState;

            @Override
            public void open(Configuration parameters) throws Exception {
                ValueStateDescriptor valueStateDescriptor = new ValueStateDescriptor("last-dt", String.class);
                valueStateDescriptor.getTtlConfig();
                valueState = getRuntimeContext().getState(valueStateDescriptor);
            }

            @Override
            public void processElement(JSONObject value, KeyedProcessFunction<String, JSONObject, JSONObject>.Context ctx, Collector<JSONObject> out) throws Exception {

                //取出来状态的值
                JSONObject lastState = valueState.value();

                //判断状态是否为 null
                if (lastState == null) {
                    valueState.update(value);
                    //注册定时器 5s
                    ctx.timerService().registerProcessingTimeTimer(ctx.timerService().currentProcessingTime() + 5000L);
                } else {
                    String stateRt = lastState.getString("row_op_ts");
                    String curRt = value.getString("row_op_ts");
                    int compare = TimestampLtz3CompareUtil.compare(stateRt, curRt);
                    if (compare != 1) {
                        valueState.update(value);
                    }
                }
            }

            @Override
            public void onTimer(long timestamp, KeyedProcessFunction<String, JSONObject, JSONObject>.OnTimerContext ctx, Collector<JSONObject> out) throws Exception {
                super.onTimer(timestamp, ctx, out);
                //输出并清空定时器里面的状态数据
                JSONObject value = valueState.value();
                out.collect(value);

                valueState.clear();
            }
        });

        //TODO 6.提取事件时间生成Watermark
        SingleOutputStreamOperator<JSONObject> jsonObjWithWmDS = processDS.assignTimestampsAndWatermarks(WatermarkStrategy.<JSONObject>forBoundedOutOfOrderness(Duration.ofSeconds(2)).withTimestampAssigner(new SerializableTimestampAssigner<JSONObject>() {
            @Override
            public long extractTimestamp(JSONObject element, long l) {
                String callbackTime = element.getString("callback_time");
                return DateFormatUtil.toTs(callbackTime, true);
            }
        }));

        //TODO 7.按照user_id分组
        KeyedStream<JSONObject, String> keyedByUidDS = jsonObjWithWmDS.keyBy(json -> json.getString("user_id"));

        //TODO 8.提取独立支付成功用户数
        SingleOutputStreamOperator<TradePaymentWindowBean> tradePaymentDS = keyedByUidDS.flatMap(new RichFlatMapFunction<JSONObject, TradePaymentWindowBean>() {

            private ValueState<String> lastDtState;

            @Override
            public void open(Configuration parameters) throws Exception {
                lastDtState = getRuntimeContext().getState(new ValueStateDescriptor<String>("last-dt", String.class));
            }

            @Override
            public void flatMap(JSONObject value, Collector<TradePaymentWindowBean> out) throws Exception {

                String lastDt = lastDtState.value();
                String curDt = value.getString("callback_time").split(" ")[0];

                //定义当日支付人数以及新增付费用户
                long paymentSucUniqueUserCount = 0L;
                long paymentSucNewUserCount = 0L;

                if (lastDt == null) {
                    paymentSucUniqueUserCount = 1L;
                    paymentSucNewUserCount = 1L;
                    lastDtState.update(curDt);
                } else if (!lastDt.equals(curDt)) {
                    paymentSucUniqueUserCount = 1L;
                    lastDtState.update(curDt);
                }

                //返回数据
                if (paymentSucUniqueUserCount == 1L) {
                    out.collect(new TradePaymentWindowBean("",
                            "",
                            paymentSucUniqueUserCount,
                            paymentSucNewUserCount,
                            null));
                }
            }
        });

        // TODO 9.开窗、聚合
        SingleOutputStreamOperator<TradePaymentWindowBean> resultDS = tradePaymentDS.windowAll(TumblingEventTimeWindows.of(Time.seconds(10)))
                .reduce(new ReduceFunction<TradePaymentWindowBean>() {
                    @Override
                    public TradePaymentWindowBean reduce(TradePaymentWindowBean value1, TradePaymentWindowBean value2) throws Exception {

                        value1.setPaymentSucNewUserCount(value1.getPaymentSucNewUserCount() + value2.getPaymentSucNewUserCount());
                        value1.setPaymentSucUniqueUserCount(value1.getPaymentSucUniqueUserCount() + value2.getPaymentSucUniqueUserCount());
                        return value1;
                    }
                }, new AllWindowFunction<TradePaymentWindowBean, TradePaymentWindowBean, TimeWindow>() {
                    @Override
                    public void apply(TimeWindow timeWindow, Iterable<TradePaymentWindowBean> value, Collector<TradePaymentWindowBean> out) throws Exception {

                        //获取数据
                        TradePaymentWindowBean next = value.iterator().next();

                        //补充信息
                        next.setStt(DateFormatUtil.toYmdHms(timeWindow.getStart()));
                        next.setEdt(DateFormatUtil.toYmdHms(timeWindow.getEnd()));
                        next.setTs(System.currentTimeMillis());

                        //输出数据
                        out.collect(next);
                    }
                });


        //TODO 10.将数据写出到ClickHouse
        resultDS.print(">>>>>>>");
        resultDS.addSink(MyClickHouseUtil.getSinkFunction("insert into dws_trade_payment_suc_window values(?,?,?,?,?)"));

        //TODO 11.启动任务
        env.execute("DwsTradePaymentSucWindow");
    }
}
