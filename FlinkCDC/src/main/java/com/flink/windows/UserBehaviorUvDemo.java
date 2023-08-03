package com.flink.windows;

import com.flink.bean.ProductUvWindow;
import com.flink.bean.UserBehavior;
import org.apache.commons.net.ntp.TimeStamp;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.MapFunction;
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
import java.util.List;

/**
 * @Author wangziyu1
 * @Date 2022/11/10 16:38
 * @Version 1.0
 * 根据提供的数据 求出每件商品在每个窗口内的浏览次数
 * 求出每个窗口内的热门商品
 */
public class UserBehaviorUvDemo {

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
                //提取事件时间
                .assignTimestampsAndWatermarks(WatermarkStrategy.<UserBehavior>forBoundedOutOfOrderness(Duration.ofSeconds(1)).withTimestampAssigner(new SerializableTimestampAssigner<UserBehavior>() {
                    @Override
                    public long extractTimestamp(UserBehavior userBehavior, long l) {
                        return userBehavior.getTimestamp();
                    }
                }))
                .keyBy(UserBehavior::getItemId)
                .window(SlidingEventTimeWindows.of(Time.hours(1), Time.minutes(5)))
                .aggregate(new CountAgg(), new WindowResult()) //到这里可以输出每个商品在各自窗口的访问总次数
                //根据窗口的结束时间再一次分组 获取每个窗口内浏览次数最多的商品
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

                        //将数据放到list里面
                        listState.add(in);
                        List<ProductUvWindow> inList = new ArrayList();
                        //取出来数据排序
                        for (ProductUvWindow e : listState.get()) inList.add(e);
                        inList.sort(new Comparator<ProductUvWindow>() {
                            @Override
                            public int compare(ProductUvWindow o1, ProductUvWindow o2) {
                                //根据浏览次数进行降序排序
                                return (int) (o2.getCount() - o1.getCount());
                            }
                        });

                        StringBuilder result = new StringBuilder();

                        result.append("================================================\n");
                        ProductUvWindow topProduct = inList.get(0);
                        result.append("窗口:" + topProduct.getWindowStartTime() + " ~ " +
                                topProduct.getWindowEndTime() + "\n");
                        result.append("第一名的数据:" + topProduct + "\n");
                        result.append("================================================\n");

                        out.collect(result.toString());

                    }
                })

                .print();

        env.execute();

    }


    private static class CountAgg implements AggregateFunction<UserBehavior, Long, Long> {
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

    private static class WindowResult extends ProcessWindowFunction<Long, ProductUvWindow, Long, TimeWindow> {
        @Override
        public void process(Long key, Context ctx, Iterable<Long> element, Collector<ProductUvWindow> out) throws Exception {

            out.collect(new ProductUvWindow(key.toString(), element.iterator().next(), ctx.window().getStart(), ctx.window().getEnd()));
        }
    }
}
