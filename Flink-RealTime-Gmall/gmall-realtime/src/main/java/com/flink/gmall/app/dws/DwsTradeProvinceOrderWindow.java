package com.flink.gmall.app.dws;

import com.alibaba.fastjson.JSONObject;
import com.flink.gmall.bean.TradeProvinceOrderWindow;
import com.flink.gmall.func.DimAsyncFunction;
import com.flink.gmall.utils.DateFormatUtil;
import com.flink.gmall.utils.MyClickHouseUtil;
import com.flink.gmall.utils.MyKafkaUtil;
import com.flink.gmall.utils.TimestampLtz3CompareUtil;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.time.Duration;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

/**
 * @Author wangziyu1
 * @Date 2022/9/5 17:40
 * @Version 1.0
 */

//数据流：Web/app -> nginx -> 业务服务器(Mysql) -> Maxwell -> Kafka(ODS) -> FlinkApp -> Kafka(DWD) -> FlinkApp -> Kafka(DWD) -> FlinkApp -> ClickHouse(DWS)
//程  序：Mock  ->  Mysql  ->  Maxwell -> Kafka(ZK)  ->  DwdTradeOrderPreProcess -> Kafka(ZK) -> DwdTradeOrderDetail -> Kafka(ZK) -> DwsTradeProvinceOrderWindow(Phoenix-(HBase-HDFS、ZK)、Redis) -> ClickHouse(ZK)
public class DwsTradeProvinceOrderWindow {

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

        //TODO 2.读取Kafka DWD层下单主题数据创建流
        String topic = "dwd_trade_order_detail";
        String groupId = "dws_trade_province_order_window_test";
        DataStreamSource<String> kafkaDS = env.addSource(MyKafkaUtil.getFlinkKafkaConsumer(topic, groupId));
        //TODO 3. 将每行数据转换为JSON对象
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaDS.flatMap(new FlatMapFunction<String, JSONObject>() {
            @Override
            public void flatMap(String value, Collector<JSONObject> out) throws Exception {
                try {
                    JSONObject jsonObject = JSONObject.parseObject(value);
                    out.collect(jsonObject);
                } catch (Exception e) {
                    System.err.println("Value>>>>>>>>" + value);
                }
            }
        });

        //TODO 4. 按照订单明细id分组 去重
        KeyedStream<JSONObject, String> keyedByDetailIdDS = jsonObjDS.keyBy(json -> json.getString("id"));

        SingleOutputStreamOperator<JSONObject> filterDS = keyedByDetailIdDS.process(new KeyedProcessFunction<String, JSONObject, JSONObject>() {


            private ValueState<JSONObject> lastState;

            @Override
            public void open(Configuration parameters) throws Exception {
                lastState = getRuntimeContext().getState(new ValueStateDescriptor<JSONObject>("last-state", JSONObject.class));
            }

            @Override
            public void processElement(JSONObject value, KeyedProcessFunction<String, JSONObject, JSONObject>.Context ctx, Collector<JSONObject> collector) throws Exception {
                //取出状态中的数据
                JSONObject lastValueState = lastState.value();

                //判断状态是否相等
                if (lastValueState == null) {
                    lastState.update(value);
                    long processingTime = ctx.timerService().currentProcessingTime();
                    ctx.timerService().registerProcessingTimeTimer(processingTime + 5000L);
                } else {
                    String lastDt = lastValueState.getString("row_op_ts");
                    String curDt = value.getString("row_op_ts");
                    if (TimestampLtz3CompareUtil.compare(lastDt, curDt) != 1) {
                        lastState.update(value);
                    }
                }
            }

            @Override
            public void onTimer(long timestamp, KeyedProcessFunction<String, JSONObject, JSONObject>.OnTimerContext ctx, Collector<JSONObject> out) throws Exception {
                //定时器触发输出 数据
                out.collect(lastState.value());
                lastState.clear(); //清空状态
            }
        });

        //TODO 5. 将每行数据转换为 javabean
        SingleOutputStreamOperator<TradeProvinceOrderWindow> provinceOrderDS = filterDS.map(line -> {

            HashSet<String> orderIdSet = new HashSet<>();
            orderIdSet.add(line.getString("order_id"));
            return new TradeProvinceOrderWindow(""
                    , "",
                    line.getString("province_id"),
                    "",
                    0L,
                    orderIdSet,
                    line.getDouble("split_total_amount"),
                    DateFormatUtil.toTs(line.getString("create_time"), true));

        });


        //TODO 6. 提取时间戳生成watermark
        SingleOutputStreamOperator<TradeProvinceOrderWindow> provinceOrderWithWmDS = provinceOrderDS.assignTimestampsAndWatermarks(WatermarkStrategy.<TradeProvinceOrderWindow>forBoundedOutOfOrderness(Duration.ofSeconds(2)).withTimestampAssigner(new SerializableTimestampAssigner<TradeProvinceOrderWindow>() {
            @Override
            public long extractTimestamp(TradeProvinceOrderWindow value, long l) {
                return value.getTs();
            }
        }));

        //TODO 7. 分组 开窗 聚合
        KeyedStream<TradeProvinceOrderWindow, String> keyedStream = provinceOrderWithWmDS.keyBy(TradeProvinceOrderWindow::getProvinceId);
        SingleOutputStreamOperator<TradeProvinceOrderWindow> reduceDS = keyedStream.window(TumblingEventTimeWindows.of(Time.seconds(10))).reduce(new ReduceFunction<TradeProvinceOrderWindow>() {
            @Override
            public TradeProvinceOrderWindow reduce(TradeProvinceOrderWindow value1, TradeProvinceOrderWindow value2) throws Exception {
                value1.getOrderIdSet().addAll(value2.getOrderIdSet());
                value1.setOrderAmount(value1.getOrderAmount() + value2.getOrderAmount());
                return value1;
            }
        }, new WindowFunction<TradeProvinceOrderWindow, TradeProvinceOrderWindow, String, TimeWindow>() {
            @Override
            public void apply(String value, TimeWindow timeWindow, Iterable<TradeProvinceOrderWindow> iterable, Collector<TradeProvinceOrderWindow> out) throws Exception {

                //获取数据
                TradeProvinceOrderWindow orderWindow = iterable.iterator().next();

                //补充数据
                orderWindow.setEdt(DateFormatUtil.toYmdHms(timeWindow.getEnd()));
                orderWindow.setStt(DateFormatUtil.toYmdHms(timeWindow.getStart()));
                orderWindow.setOrderCount((long) orderWindow.getOrderIdSet().size());
                orderWindow.setTs(System.currentTimeMillis());

                //输出数据
                out.collect(orderWindow);
            }
        });

        //TODO 8. 关联省份维表补充省份名称字段
        SingleOutputStreamOperator<TradeProvinceOrderWindow> reduceWithProvinceDS = AsyncDataStream.unorderedWait(reduceDS, new DimAsyncFunction<TradeProvinceOrderWindow>("DIM_BASE_PROVINCE") {
            @Override
            public String getKey(TradeProvinceOrderWindow input) {
                return input.getProvinceId();
            }

            @Override
            public void join(TradeProvinceOrderWindow input, JSONObject dimInfo) {
                input.setProvinceName(dimInfo.getString("name"));
            }
        }, 100, TimeUnit.SECONDS);

        reduceWithProvinceDS.print("reduceWithProvinceDS>>>>>>>>>>>");
        reduceWithProvinceDS.addSink(MyClickHouseUtil.getSinkFunction("insert into dws_trade_province_order_window values(?,?,?,?,?,?,?)"));

        //TODO 10.启动任务
        env.execute("DwsTradeProvinceOrderWindow");

    }

}
