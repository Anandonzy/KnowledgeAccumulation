package com.flink.windows;

import com.flink.api.soruce.ClickSource;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

/**
 * @Author wangziyu1
 * @Date 2022/11/8 19:44
 * @Version 1.0
 * <p>
 * AggregateFunction 和 ProcessWindowFunction 结合使用
 * AggregateFunction 预聚合 窗口内的数据 最后窗口关闭的时候调用getResult()方法 然后传递给ProcessWindowFunction
 */
public class UserViewCountAggAndProcessDemo {


    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        env.addSource(new ClickSource())
                .keyBy(ClickSource.Click::getName)
                .window(TumblingProcessingTimeWindows.of(Time.seconds(10)))
                .aggregate(new AggregateFunction<ClickSource.Click, Long, Long>() {
                    //初始化累加器
                    @Override
                    public Long createAccumulator() {
                        return 0L;
                    }

                    //没来一条加1
                    @Override
                    public Long add(ClickSource.Click click, Long acc) {
                        return acc + 1L;
                    }

                    //最后窗口结束的时候调用getResult方法
                    @Override
                    public Long getResult(Long acc) {
                        return acc;
                    }

                    @Override
                    public Long merge(Long aLong, Long acc1) {
                        return null;
                    }
                }, new ProcessWindowFunction<Long, UserViewCountPreWindow, String, TimeWindow>() {
                    @Override
                    public void process(String key, Context ctx, Iterable<Long> elements, Collector<UserViewCountPreWindow> out) throws Exception {

                        out.collect(new UserViewCountPreWindow(key, elements.iterator().next(),
                                ctx.window().getStart(),
                                ctx.window().getEnd()));
                    }
                })
                .print();


        env.execute("AggregateFunctionAndProcessWindowFunctionUsed");


    }
}
