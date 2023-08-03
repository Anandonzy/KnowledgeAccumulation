package com.study.wc;

import com.flink.bean.ProductUvWindow;
import com.flink.bean.UserBehavior;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
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
import org.apache.flink.util.Collector;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * @Author wangziyu1
 * @Date 2022/11/11 09:54
 * @Version 1.0
 * 1.先根据商品id找出每个窗口内浏览次数最多的
 * 2.然后找出同一个窗口内最热门的商品top3
 */
public class UserBehaviorUvOptimizeDemo {


    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);

        env.readTextFile("/Users/wangziyu/Desktop/UserBehavior.csv")
                .flatMap(new FlatMapFunction<String, UserBehavior>() {
                    @Override
                    public void flatMap(String in, Collector<UserBehavior> out) throws Exception {
                        String[] value = in.split(",");

                        //注意时间戳要转化
                        UserBehavior userBehavior = new UserBehavior(Long.parseLong(value[0]), Long.parseLong(value[1])
                                , Integer.parseInt(value[2]), value[3], Long.parseLong(value[4]) * 1000L);


                        if (userBehavior.getBehavior().equals("pv")) {
                            out.collect(userBehavior);
                        }
                    }
                })
                .assignTimestampsAndWatermarks(WatermarkStrategy.<UserBehavior>forBoundedOutOfOrderness(Duration.ofSeconds(0)).withTimestampAssigner(new SerializableTimestampAssigner<UserBehavior>() {
                    @Override
                    public long extractTimestamp(UserBehavior userBehavior, long l) {
                        return userBehavior.getTimestamp();
                    }
                }))
                .keyBy(UserBehavior::getItemId)
                .window(SlidingEventTimeWindows.of(Time.hours(1), Time.minutes(5)))
                .aggregate(new AccCount(), new ResultCount())
                .keyBy(ProductUvWindow::getWindowEndTime)
                .process(new KeyedProcessFunction<Long, ProductUvWindow, String>() {
                    private ListState<ProductUvWindow> listState;

                    @Override
                    public void open(Configuration parameters) throws Exception {
                        listState = getRuntimeContext().getListState(new ListStateDescriptor<ProductUvWindow>("listState",
                                Types.POJO(ProductUvWindow.class)));
                    }

                    @Override
                    public void processElement(ProductUvWindow in, Context ctx, Collector<String> out) throws Exception {
                        //每一条数据 都放到键控状态里面
                        listState.add(in);

                        //注册定时器 保证窗口的数据都能到齐 然后再排序
                        ctx.timerService().registerEventTimeTimer(in.getWindowEndTime() + 1000L);
                    }

                    @Override
                    public void onTimer(long timerTs, OnTimerContext ctx, Collector<String> out) throws Exception {
                        ArrayList<ProductUvWindow> inList = new ArrayList<>();
                        for (ProductUvWindow e : listState.get()) {
                            inList.add(e);
                        }

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
                        result.append("热门的商品top" + 3 + "\n" + "窗口的结束时间是:" + (timerTs - 1000L) + "\n");

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


    private static class AccCount implements AggregateFunction<UserBehavior, Long, Long> {
        @Override
        public Long createAccumulator() {
            return 0L;
        }

        @Override
        public Long add(UserBehavior userBehavior, Long acc) {
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
    }

    //获取增量聚合结果输出
    private static class ResultCount extends ProcessWindowFunction<Long, ProductUvWindow, Long, TimeWindow> {

        @Override
        public void process(Long key, Context ctx, Iterable<Long> elements, Collector<ProductUvWindow> out) throws Exception {

            out.collect(new ProductUvWindow(key.toString(),
                    elements.iterator().next(),
                    ctx.window().getStart(),
                    ctx.window().getEnd()
            ));
        }
    }
}
