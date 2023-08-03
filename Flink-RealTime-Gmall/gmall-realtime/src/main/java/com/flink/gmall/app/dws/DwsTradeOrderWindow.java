package com.flink.gmall.app.dws;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.flink.gmall.bean.TradeOrderBean;
import com.flink.gmall.utils.DateFormatUtil;
import com.flink.gmall.utils.MyClickHouseUtil;
import com.flink.gmall.utils.MyKafkaUtil;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.*;
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
 * @Date 2022/8/31 13:36
 * @Version 1.0
 * 处理重复的数据的方法二 使用filter 保存第一条数据 ,第二条直接不要了.
 * 针对order_detail id去重(保留第一条数据即可)
 */
//数据流：Web/app -> nginx -> 业务服务器(Mysql) -> Maxwell -> Kafka(ODS) -> FlinkApp -> Kafka(DWD) -> FlinkApp -> Kafka(DWD) -> FlinkApp -> ClickHouse(DWS)
//程  序：Mock  ->  Mysql  ->  Maxwell -> Kafka(ZK)  ->  DwdTradeOrderPreProcess -> Kafka(ZK) -> DwdTradeOrderDetail -> Kafka(ZK) -> DwsTradeOrderWindow -> ClickHouse(ZK)
public class DwsTradeOrderWindow {

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

        //TODO 2.读取kafka dwd层下单主题数据创建流
        String topic = "dwd_trade_order_detail";
        String groupId = "dws_trade_order_window_test";
        DataStreamSource<String> kafkaDS = env.addSource(MyKafkaUtil.getFlinkKafkaConsumer(topic, groupId));

        //TODO 3. 转换数据为JSON对象
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaDS.flatMap(new FlatMapFunction<String, JSONObject>() {
            @Override
            public void flatMap(String value, Collector<JSONObject> collector) throws Exception {
                try {
                    JSONObject jsonObject = JSON.parseObject(value);
                    collector.collect(jsonObject);
                } catch (Exception e) {
                    System.out.println(">>>>" + value);
                }
            }
        });

        //TODO 4.按照order_detail_id 分组
        KeyedStream<JSONObject, String> keyedByDetailIDS = jsonObjDS.keyBy(json -> json.getString("id"));

        //TODO 5. 针对order_detail id去重(保留第一条数据即可)
        SingleOutputStreamOperator<JSONObject> filterDS = keyedByDetailIDS.filter(new RichFilterFunction<JSONObject>() {

            private ValueState<String> valueState;

            @Override
            public void open(Configuration parameters) throws Exception {
                //设置状态ttl 5s
                StateTtlConfig ttlConfig = new StateTtlConfig.Builder(Time.seconds(5))
                        .setUpdateType(StateTtlConfig.UpdateType.OnReadAndWrite)
                        .build();
                ValueStateDescriptor<String> valueStateDescriptor = new ValueStateDescriptor<>("is-exists", String.class);
                valueStateDescriptor.enableTimeToLive(ttlConfig);
                valueState = getRuntimeContext().getState(valueStateDescriptor);
            }

            @Override
            public boolean filter(JSONObject jsonObject) throws Exception {

                //获取状态数据
                String value = valueState.value();

                //判断状态是否为null
                if (value == null) {
                    valueState.update("1"); //设置个值 保证一下条不是null  如果超过5s会自动根据ttl清除数据
                    return true;
                } else {
                    return false;
                }
            }
        });

        //TODO 6. 提取事件时间生成wm
        SingleOutputStreamOperator<JSONObject> jsonObjWithWmDS = filterDS.assignTimestampsAndWatermarks(WatermarkStrategy.<JSONObject>forBoundedOutOfOrderness(Duration.ofSeconds(2)).withTimestampAssigner(new SerializableTimestampAssigner<JSONObject>() {
            @Override
            public long extractTimestamp(JSONObject value, long l) {
                return DateFormatUtil.toTs(value.getString("create_time"), true);
            }
        }));

        //TODO 7.按照user_id分组
        KeyedStream<JSONObject, String> keyedByUidDS = jsonObjWithWmDS.keyBy(json -> json.getString("user_id"));

        //TODO 8.提取独立下单用户
        SingleOutputStreamOperator<TradeOrderBean> tradeOrderDS = keyedByUidDS.map(new RichMapFunction<JSONObject, TradeOrderBean>() {

            private ValueState<String> valueState;

            @Override
            public void open(Configuration parameters) throws Exception {
                valueState = getRuntimeContext().getState(new ValueStateDescriptor<String>("last-order", String.class));
            }

            @Override
            public TradeOrderBean map(JSONObject jsonObject) throws Exception {
                //获取状态中日期以及当前数据的日期
                String lastOrderDt = valueState.value();
                String curDt = jsonObject.getString("create_time").split(" ")[0];

                //定义当天下单人数以及新增下单人数
                long orderUniqueUserCount = 0L;
                long orderNewUserCount = 0L;

                if (lastOrderDt == null) {
                    orderUniqueUserCount = 1L;
                    orderNewUserCount = 1L;
                    valueState.update(curDt);
                } else if (!curDt.equals(lastOrderDt)) {
                    orderUniqueUserCount = 1L;

                    valueState.update(curDt);
                }

                //取出下单件数以及单价
                Integer skuNum = jsonObject.getInteger("sku_num");
                Double orderPrice = jsonObject.getDouble("order_price");
                Double splitActivityAmount = jsonObject.getDouble("split_activity_amount");
                //处理金额字段为null 的情况 赋值为0.0
                if (splitActivityAmount == null) {
                    splitActivityAmount = 0.0D;
                }
                Double splitCouponAmount = jsonObject.getDouble("split_coupon_amount");
                if (splitCouponAmount == null) {
                    splitCouponAmount = 0.0D;
                }

                return new TradeOrderBean("", "",
                        orderUniqueUserCount,
                        orderNewUserCount,
                        splitActivityAmount,
                        splitCouponAmount,
                        skuNum * orderPrice,
                        null);
            }
        });

        //TODO 9.开窗 聚合
        SingleOutputStreamOperator<TradeOrderBean> resultDS = tradeOrderDS.windowAll(TumblingEventTimeWindows.of(org.apache.flink.streaming.api.windowing.time.Time.seconds(10)))
                .reduce(new ReduceFunction<TradeOrderBean>() {
                    @Override
                    public TradeOrderBean reduce(TradeOrderBean value1, TradeOrderBean value2) throws Exception {
                        value1.setOrderUniqueUserCount(value1.getOrderUniqueUserCount() + value2.getOrderUniqueUserCount());
                        value1.setOrderNewUserCount(value1.getOrderNewUserCount() + value2.getOrderNewUserCount());
                        value1.setOrderOriginalTotalAmount(value1.getOrderOriginalTotalAmount() + value2.getOrderOriginalTotalAmount());
                        value1.setOrderActivityReduceAmount(value1.getOrderActivityReduceAmount() + value2.getOrderActivityReduceAmount());
                        value1.setOrderCouponReduceAmount(value1.getOrderCouponReduceAmount() + value2.getOrderCouponReduceAmount());
                        return value1;
                    }
                }, new AllWindowFunction<TradeOrderBean, TradeOrderBean, TimeWindow>() {
                    @Override
                    public void apply(TimeWindow timeWindow, Iterable<TradeOrderBean> values, Collector<TradeOrderBean> out) throws Exception {
                        //获取数据
                        TradeOrderBean tradeOrderBean = values.iterator().next();

                        //补充信息
                        tradeOrderBean.setStt(DateFormatUtil.toYmdHms(timeWindow.getStart()));
                        tradeOrderBean.setEdt(DateFormatUtil.toYmdHms(timeWindow.getEnd()));
                        tradeOrderBean.setTs(System.currentTimeMillis());

                        //输出数据
                        out.collect(tradeOrderBean);
                    }
                });


        //TODO 10.将数据写出到ClickHouse
        resultDS.print(">>>>>>>>>>>");
        resultDS.addSink(MyClickHouseUtil.getSinkFunction("insert into dws_trade_order_window values(?,?,?,?,?,?,?,?)"));

        //TODO 11.启动任务
        env.execute("DwsTradeOrderWindow");

    }
}
