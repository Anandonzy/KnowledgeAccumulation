package com.flink.windows;

import com.flink.bean.ProductUvWindow;
import com.flink.bean.UserBehavior;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.scala.typeutils.Types;
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
import java.util.List;

/**
 * @Author wangziyu1
 * @Date 2022/11/10 19:03
 * @Version 1.0
 * 根据提供的数据 求出每件商品在每个窗口内的浏览次数
 * 求出每个窗口内的热门商品
 */
public class UserBehaviorUvOptimizeDemo {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);
        env.readTextFile("/Users/wangziyu/Desktop/UserBehavior.csv")
                .map(new MapFunction<String, UserBehavior>() {
                    @Override
                    public UserBehavior map(String in) throws Exception {
                        String[] value = in.split(",");

                        return new UserBehavior(Long.parseLong(value[0]), Long.parseLong(value[1])
                                , Integer.parseInt(value[2]), value[3], Long.parseLong(value[4]) * 1000L);
                    }
                })
                //过滤浏览数据
                .filter(r -> "pv".equals(r.getBehavior()))
                .assignTimestampsAndWatermarks(WatermarkStrategy.<UserBehavior>forBoundedOutOfOrderness(Duration.ofSeconds(1)).withTimestampAssigner(new SerializableTimestampAssigner<UserBehavior>() {
                    @Override
                    public long extractTimestamp(UserBehavior userBehavior, long l) {
                        return userBehavior.getTimestamp();
                    }
                }))
                .keyBy(UserBehavior::getItemId)
                .window(SlidingEventTimeWindows.of(Time.hours(1), Time.minutes(5))) //每隔5min看一下过去一个小时的数据情况
                .aggregate(new AggregateFunction<UserBehavior, Long, Long>() {
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
                }, new ProcessWindowFunction<Long, ProductUvWindow, Long, TimeWindow>() {
                    @Override
                    public void process(Long key, Context ctx, Iterable<Long> elements, Collector<ProductUvWindow> out) throws Exception {
                        out.collect(new ProductUvWindow(key.toString(), elements.iterator().next()
                                , ctx.window().getStart(), ctx.window().getEnd()));
                    }
                })
                .keyBy(ProductUvWindow::getWindowEndTime)
                .process(new TopN(3))
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
                    "listState", Types.POJO(ProductUvWindow.class)));
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
