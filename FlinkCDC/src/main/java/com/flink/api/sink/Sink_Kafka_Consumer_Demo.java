package com.flink.api.sink;

import com.flink.bean.ProductUvWindow;
import com.flink.bean.UserBehavior;
import org.apache.commons.net.ntp.TimeStamp;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

/**
 * @Author wangziyu1
 * @Date 2022/11/14 10:38
 * @Version 1.0
 */
public class Sink_Kafka_Consumer_Demo {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);

        Properties properties = new Properties();
        properties.put("bootstrap.servers", "192.168.200.144:9092");
        properties.put("group.id", "test");
        properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("auto.offset.reset", "latest");

        env.addSource(new FlinkKafkaConsumer<String>("topic_sensor", new SimpleStringSchema(), properties))
                //env.readTextFile("/Users/wangziyu/Desktop/UserBehavior.csv")
                .flatMap(new FlatMapFunction<String, UserBehavior>() {
                    @Override
                    public void flatMap(String in, Collector<UserBehavior> out) throws Exception {
                        String[] split = in.split(",");
                        UserBehavior userBehavior = new UserBehavior(Long.valueOf(split[0]), Long.valueOf(split[1]), Integer.valueOf(split[2]), split[3], Long.valueOf(split[4]));

                        if (userBehavior.getBehavior().equals("pv")) {
                            out.collect(userBehavior);
                        }
                    }
                })
                //forMonotonousTimestamps 语法糖 将延迟时间设置为0
                .assignTimestampsAndWatermarks(WatermarkStrategy.<UserBehavior>forBoundedOutOfOrderness(Duration.ofSeconds(0)).withTimestampAssigner(new SerializableTimestampAssigner<UserBehavior>() {
                    @Override
                    public long extractTimestamp(UserBehavior userBehavior, long l) {
                        return userBehavior.getTimestamp();
                    }
                }))
                .keyBy(UserBehavior::getItemId)
                .window(SlidingEventTimeWindows.of(Time.hours(1), Time.minutes(5)))
                .aggregate(new AggregateFunction<UserBehavior, Long, Long>() {
                    @Override
                    public Long createAccumulator() {
                        return 0L;
                    }

                    @Override
                    public Long add(UserBehavior in, Long acc) {
                        return acc + 1L;
                    }

                    @Override
                    public Long getResult(Long acc) {
                        return acc;
                    }

                    @Override
                    public Long merge(Long aLong, Long acc1) {
                        return null;
                    }
                }, new ProcessWindowFunction<Long, ProductUvWindow, Long, TimeWindow>() {
                    @Override
                    public void process(Long key, Context ctx, Iterable<Long> elements, Collector<ProductUvWindow> out) throws Exception {

                        out.collect(new ProductUvWindow(key.toString(),
                                elements.iterator().next(),
                                ctx.window().getStart(),
                                ctx.window().getEnd()));
                    }
                })

                .keyBy(ProductUvWindow::getWindowEndTime)
                .process(new KeyedProcessFunction<Long, ProductUvWindow, String>() {


                    private ListState<ProductUvWindow> listState;

                    @Override
                    public void open(Configuration parameters) throws Exception {

                        listState = getRuntimeContext().getListState(
                                new ListStateDescriptor<ProductUvWindow>("listState",
                                        Types.POJO(ProductUvWindow.class)));
                    }

                    @Override
                    public void processElement(ProductUvWindow in, Context ctx, Collector<String> out) throws Exception {

                        listState.add(in);

                        //当所有的窗口数据到齐之后在排序计算
                        ctx.timerService().registerEventTimeTimer(in.getWindowEndTime() + 1000L);
                    }

                    @Override
                    public void onTimer(long timerTs, OnTimerContext ctx, Collector<String> out) throws Exception {

                        ArrayList<ProductUvWindow> inList = new ArrayList<>();

                        //取出来数据
                        for (ProductUvWindow e : listState.get()) inList.add(e);

                        //清理缓存
                        listState.clear();

                        inList.sort(new Comparator<ProductUvWindow>() {
                            @Override
                            public int compare(ProductUvWindow o1, ProductUvWindow o2) {
                                return (int) (o2.getCount() - o1.getCount());
                            }
                        });

                        //输出结果
                        StringBuilder result = new StringBuilder();


                        result.append("----------------------------------------------------------------\n");
                        result.append("热门的商品top" + 3 + "\n" + "窗口的结束时间是:" + new Timestamp(timerTs - 1000L) + "\n");

                        for (int i = 0; i < 3; i++) {
                            ProductUvWindow productUvWindow = inList.get(i);
                            result.append("商品第" + (i + 1) + "的商品id是:" + productUvWindow.getProductId() + ",浏览的次数是:" + productUvWindow.getCount() + "\n");
                        }
                        result.append("----------------------------------------------------------------\n");
                        out.collect(result.toString());

                    }
                })
                .print();


        env.execute();


    }

    private static class TopN extends KeyedProcessFunction<Long, ProductUvWindow, String> {

        private final int topN;

        public TopN(int topN) {
            this.topN = topN;
        }

        private ListState<ProductUvWindow> listState;

        @Override

        public void open(Configuration parameters) throws Exception {
            listState = getRuntimeContext().getListState(new ListStateDescriptor<ProductUvWindow>(
                    "listState", org.apache.flink.api.scala.typeutils.Types.POJO(ProductUvWindow.class)));
        }

        @Override
        public void processElement(ProductUvWindow in, Context ctx, Collector<String> out) throws Exception {
            listState.add(in);

            //在窗口 windowEndTime 全部到达之后再排序输出 保证同一个窗口的数据全部到达
            ctx.timerService().registerEventTimeTimer(in.windowEndTime + 1000L);
        }

        @Override
        public void onTimer(long timerTs, OnTimerContext ctx, Collector<String> out) throws Exception {
            List<ProductUvWindow> inList = new ArrayList<>();

            for (ProductUvWindow e : listState.get()) inList.add(e);
            listState.clear(); //取出来了了 然后清空

            //排序
            inList.sort(new Comparator<ProductUvWindow>() {
                @Override
                public int compare(ProductUvWindow o1, ProductUvWindow o2) {
                    return (int) (o2.getCount() - o1.getCount());
                }
            });

            //输出结果
            StringBuilder result = new StringBuilder();


            result.append("----------------------------------------------------------------\n");
            result.append("热门的商品top" + topN + "\n" + "窗口的结束时间是:" + (timerTs - 1000L) + "\n");

            for (int i = 0; i < topN; i++) {
                ProductUvWindow productUvWindow = inList.get(i);
                result.append("商品第" + (i + 1) + "的商品id是:" + productUvWindow.getProductId() + ",浏览的次数是:" + productUvWindow.getCount() + "\n");
            }
            result.append("----------------------------------------------------------------\n");
            out.collect(result.toString());

        }
    }

}
