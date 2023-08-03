package com.flink.gmall.app.dws;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.flink.gmall.bean.TradeUserSpuOrderBean;
import com.flink.gmall.utils.*;
import com.flink.gmall.func.DimAsyncFunction;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.functions.RichFilterFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.time.Duration;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

/**
 * @Author wangziyu1
 * @Date 2022/9/1 10:03
 * @Version 1.0
 * 6.9 交易域用户-SPU粒度下单各窗口汇总表
 * 最重要的一个需求 结合很多知识点
 */

//数据流：Web/app -> nginx -> 业务服务器(Mysql) -> Maxwell -> Kafka(ODS) -> FlinkApp -> Kafka(DWD) -> FlinkApp -> Kafka(DWD) -> FlinkApp -> ClickHouse(DWS)
//程  序：Mock  ->  Mysql  ->  Maxwell -> Kafka(ZK)  ->  DwdTradeOrderPreProcess -> Kafka(ZK) -> DwdTradeOrderDetail -> Kafka(ZK) -> DwsTradeUserSpuOrderWindow(Phoenix-(HBase-HDFS、ZK)、Redis) -> ClickHouse(ZK)
public class DwsTradeUserSpuOrderWindow {

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
        String groupId = "dws_trade_user_spu_order_window_test";
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

        //TODO 6.将数据转换为JavaBean对象

        SingleOutputStreamOperator<TradeUserSpuOrderBean> tradeUserSpuDS = filterDS.map(json -> {
            HashSet<String> orderIdSet = new HashSet<>();
            orderIdSet.add(json.getString("order_id"));
            return TradeUserSpuOrderBean.builder()
                    .skuId(json.getString("sku_id"))
                    .userId(json.getString("user_id"))
                    .orderAmount(json.getDouble("split_total_amount"))
                    .orderIdSet(orderIdSet)
                    .ts(DateFormatUtil.toTs(json.getString("create_time"), true))
                    .build();
        });

        tradeUserSpuDS.print(">>>>>>> ");

        //TODO 7. 关联sku_info 维表 spu_id,tm_id,category3_id
        //tradeUserSpuDS.map(new RichMapFunction<TradeUserSpuOrderBean, TradeUserSpuOrderBean>() {
        //
        //    private DruidDataSource dataSource;
        //
        //    @Override
        //    public void open(Configuration parameters) throws Exception {
        //        //创建 phoenix 连接池
        //        DruidDataSource dataSource = DruidDSUtil.createDataSource();
        //
        //    }
        //
        //    @Override
        //    public TradeUserSpuOrderBean map(TradeUserSpuOrderBean tradeUserSpuOrderBean) throws Exception {
        //        //查询维表信息 将查询到的信息 补充到bean中
        //        DruidPooledConnection connection = dataSource.getConnection();
        //
        //        //创建一个工具类 查询维表
        //        // select * from dim_info_sku 查询维度信息
        //
        //        DimUtil.getDimInfo(connection, "DIM_SKU_INFO", tradeUserSpuOrderBean.getUserId());
        //        return null;
        //    }
        //});

        //由于关联维表使用map 性能会不高 这边使用异步IO关联维表 可以提高效率
        //map函数没法使用多线程 使用异步io 可以使用线程池
        SingleOutputStreamOperator<TradeUserSpuOrderBean> tradeUserSpuWithSkuDS = AsyncDataStream.unorderedWait(tradeUserSpuDS, new DimAsyncFunction<TradeUserSpuOrderBean>("DIM_SKU_INFO") {
                    @Override
                    public String getKey(TradeUserSpuOrderBean input) {
                        return input.getSkuId();
                    }

                    @Override
                    public void join(TradeUserSpuOrderBean input, JSONObject dimInfo) {
                        input.setSpuId(dimInfo.getString("spu_id"));
                        input.setTrademarkId(dimInfo.getString("tm_id"));
                        input.setCategory3Id(dimInfo.getString("category3_id"));
                    }
                },
                100, TimeUnit.SECONDS);

        tradeUserSpuWithSkuDS.print("tradeUserSpuWithSkuDS>>>>>>>>>>>");


        //TODO 8. 提取事件时间 生成wm
        SingleOutputStreamOperator<TradeUserSpuOrderBean> tradeUserSpuOrderWithWmDS = tradeUserSpuWithSkuDS.assignTimestampsAndWatermarks(WatermarkStrategy.<TradeUserSpuOrderBean>forBoundedOutOfOrderness(Duration.ofSeconds(2)).withTimestampAssigner(new SerializableTimestampAssigner<TradeUserSpuOrderBean>() {
            @Override
            public long extractTimestamp(TradeUserSpuOrderBean value, long l) {
                return value.getTs();
            }
        }));

        //TODO 9. 分组,开窗,聚合
        KeyedStream<TradeUserSpuOrderBean, Tuple4<String, String, String, String>> tradeUserSpuOrderKeyDS = tradeUserSpuOrderWithWmDS.keyBy(new KeySelector<TradeUserSpuOrderBean, Tuple4<String, String, String, String>>() {
            @Override
            public Tuple4<String, String, String, String> getKey(TradeUserSpuOrderBean value) throws Exception {
                return new Tuple4<>(value.getUserId()
                        , value.getSpuId()
                        , value.getTrademarkId()
                        , value.getCategory3Id());
            }
        });
        SingleOutputStreamOperator<TradeUserSpuOrderBean> reduceDS = tradeUserSpuOrderKeyDS.windowAll(TumblingEventTimeWindows.of(org.apache.flink.streaming.api.windowing.time.Time.seconds(10))).reduce(new ReduceFunction<TradeUserSpuOrderBean>() {
            @Override
            public TradeUserSpuOrderBean reduce(TradeUserSpuOrderBean value1, TradeUserSpuOrderBean value2) throws Exception {
                value1.getOrderIdSet().addAll(value2.getOrderIdSet());
                value1.setOrderAmount(value1.getOrderAmount() + value2.getOrderAmount());
                return value1;
            }
        }, new AllWindowFunction<TradeUserSpuOrderBean, TradeUserSpuOrderBean, TimeWindow>() {
            @Override
            public void apply(TimeWindow timeWindow, Iterable<TradeUserSpuOrderBean> value, Collector<TradeUserSpuOrderBean> out) throws Exception {
                //获取数据
                TradeUserSpuOrderBean userSpuOrderBean = value.iterator().next();
                Iterable<Tuple2<String ,Integer>> values ;

                //补充信息
                userSpuOrderBean.setStt(DateFormatUtil.toYmdHms(timeWindow.getStart()));
                userSpuOrderBean.setEdt(DateFormatUtil.toYmdHms(timeWindow.getEnd()));
                userSpuOrderBean.setTs(System.currentTimeMillis());
                userSpuOrderBean.setOrderCount((long) userSpuOrderBean.getOrderIdSet().size());

                //发出数据
                out.collect(userSpuOrderBean);
            }
        });

        //TODO 10.关联spu tm category 补充维表相应的信息
        //10.1 关联SPU表
        SingleOutputStreamOperator<TradeUserSpuOrderBean> reduceWithSpuDS = AsyncDataStream.unorderedWait(reduceDS, new DimAsyncFunction<TradeUserSpuOrderBean>("DIM_SPU_INFO") {
            @Override
            public String getKey(TradeUserSpuOrderBean input) {
                return input.getSpuId();
            }

            @Override
            public void join(TradeUserSpuOrderBean input, JSONObject dimInfo) {
                input.setSpuName(dimInfo.getString("spu_name"));

            }
        }, 100, TimeUnit.SECONDS);

        //10.2 关联Tm表
        SingleOutputStreamOperator<TradeUserSpuOrderBean> reduceWithTmDS = AsyncDataStream.unorderedWait(reduceWithSpuDS, new DimAsyncFunction<TradeUserSpuOrderBean>("DIM_BASE_TRADEMARK") {
            @Override
            public String getKey(TradeUserSpuOrderBean input) {
                return input.getTrademarkId();
            }

            @Override
            public void join(TradeUserSpuOrderBean input, JSONObject dimInfo) {
                input.setTrademarkName(dimInfo.getString("tm_name"));
            }
        }, 100, TimeUnit.SECONDS);


        //10.3 关联Category3
        SingleOutputStreamOperator<TradeUserSpuOrderBean> reduceWithCategory3DS = AsyncDataStream.unorderedWait(reduceWithTmDS, new DimAsyncFunction<TradeUserSpuOrderBean>("DIM_BASE_CATEGORY3") {
            @Override
            public String getKey(TradeUserSpuOrderBean input) {
                return input.getCategory3Id();
            }

            @Override
            public void join(TradeUserSpuOrderBean input, JSONObject dimInfo) {
                input.setCategory3Name(dimInfo.getString("name"));
                input.setCategory2Id(dimInfo.getString("category2_id"));
            }
        }, 100, TimeUnit.SECONDS);

        //10.4 关联Category2
        SingleOutputStreamOperator<TradeUserSpuOrderBean> reduceWihCategory2DS = AsyncDataStream.unorderedWait(reduceWithCategory3DS, new DimAsyncFunction<TradeUserSpuOrderBean>("DIM_BASE_CATEGORY2") {
            @Override
            public String getKey(TradeUserSpuOrderBean input) {
                return input.getCategory2Id();
            }

            @Override
            public void join(TradeUserSpuOrderBean input, JSONObject dimInfo) {
                input.setCategory2Name(dimInfo.getString("name"));
                input.setCategory1Id(dimInfo.getString("category1_id"));

            }
        }, 100, TimeUnit.SECONDS);
        //10.5 关联Category1
        SingleOutputStreamOperator<TradeUserSpuOrderBean> reduceWithCategory1DS = AsyncDataStream.unorderedWait(reduceWihCategory2DS, new DimAsyncFunction<TradeUserSpuOrderBean>("DIM_BASE_CATEGORY1") {
            @Override
            public String getKey(TradeUserSpuOrderBean input) {
                return input.getCategory1Id();
            }

            @Override
            public void join(TradeUserSpuOrderBean input, JSONObject dimInfo) {
                input.setCategory1Name(dimInfo.getString("name"));
            }
        }, 100, TimeUnit.SECONDS);

        //TODO 11.将数据写出到ClickHouse
        reduceWithCategory1DS.print(">>>>>>>>>>>>>>>>>");
        reduceWithCategory1DS.addSink(MyClickHouseUtil.getSinkFunction("insert into dws_trade_user_spu_order_window values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"));

        //TODO 12.启动
        env.execute("DwsTradeUserSpuOrderWindow");




    }

}
