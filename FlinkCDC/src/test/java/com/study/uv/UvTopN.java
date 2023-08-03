package com.study.uv;

import com.flink.bean.ProductUvWindow;
import com.flink.bean.UserBehavior;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.util.Collector;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @Author wangziyu1
 * @Date 2023/2/9 16:08
 * @Version 1.0
 * 根据提供的数据 求出每件商品在每个窗口内的浏览次数
 * 求出每个窗口内的热门商品
 */
public class UvTopN {

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
                .assignTimestampsAndWatermarks(WatermarkStrategy.<UserBehavior>forBoundedOutOfOrderness(Duration.ofSeconds(2)).withTimestampAssigner(new SerializableTimestampAssigner<UserBehavior>() {
                    @Override
                    public long extractTimestamp(UserBehavior element, long recordTimestamp) {
                        return element.getItemId();
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
                    public Long merge(Long a, Long b) {
                        return null;
                    }
                }, new ProcessWindowFunction<Long, ProductUvWindow, Long, TimeWindow>() {

                    @Override
                    public void process(Long key, Context ctx, Iterable<Long> element, Collector<ProductUvWindow> out) throws Exception {

                        out.collect(new ProductUvWindow(key.toString(), element.iterator().next(), ctx.window().getStart(), ctx.window().getEnd()));

                    }
                })
                .keyBy(ProductUvWindow::getWindowEndTime)
                .process(new KeyedProcessFunction<Long, ProductUvWindow, String>() {

                    private ListState<ProductUvWindow> listState;

                    @Override
                    public void open(Configuration parameters) throws Exception {
                        listState = getRuntimeContext().getListState(new ListStateDescriptor<ProductUvWindow>("listState", Types.POJO(ProductUvWindow.class)));
                    }

                    @Override
                    public void processElement(ProductUvWindow value, Context ctx, Collector<String> out) throws Exception {

                        listState.add(value);

                        //注册个定时器 窗口的结束时间 保证窗口的数据全部到齐
                        ctx.timerService().registerEventTimeTimer(value.getWindowEndTime());

                    }

                    @Override
                    public void onTimer(long timerTs, OnTimerContext ctx, Collector<String> out) throws Exception {

                        //闹钟响了 数据到齐了 取出来数据排序
                        Iterable<ProductUvWindow> iterable = listState.get();
                        List<ProductUvWindow> list = new ArrayList<>();
                        iterable.forEach(list::add); //全部取出来

                        list.sort(new Comparator<ProductUvWindow>() {
                            @Override
                            public int compare(ProductUvWindow o1, ProductUvWindow o2) {
                                return (int) (o2.getCount() - o1.getCount());
                            }
                        });
                        listState.clear();


                        StringBuilder result = new StringBuilder();

                        result.append("================================================\n");
                        ProductUvWindow topProduct = list.get(0);
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
}
