package com.flink.windows;

import com.flink.api.soruce.ClickSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

/**
 * @Author wangziyu1
 * @Date 2022/11/8 13:47
 * @Version 1.0
 */
public class UserViewCountProcessWindowFunctionProcessTimeDemo {

    public static void main(String[] args) throws Exception {


        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        env.addSource(new ClickSource())
                .keyBy(ClickSource.Click::getName)
                .window(TumblingProcessingTimeWindows.of(Time.seconds(10)))
                .process(new ProcessWindowFunction<ClickSource.Click, UserViewCountPreWindow, String, TimeWindow>() {
                    @Override
                    public void process(String key, Context ctx, Iterable<ClickSource.Click> elelments, Collector<UserViewCountPreWindow> out) throws Exception {

                        out.collect(new UserViewCountPreWindow(key,
                                elelments.spliterator().getExactSizeIfKnown(),
                                ctx.window().getStart(), ctx.window().getEnd()));
                    }
                }).print();

        env.execute("UserViewCountProcessWindowFunctionEventTimeDemo");
    }


}
