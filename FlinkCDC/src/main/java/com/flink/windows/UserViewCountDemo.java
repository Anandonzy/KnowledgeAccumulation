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
 * @Date 2022/11/8 11:44
 * @Version 1.0
 */
public class UserViewCountDemo {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);
        env.addSource(new ClickSource())
                .keyBy(ClickSource.Click::getName)
                .window(TumblingProcessingTimeWindows.of(Time.seconds(10)))
                .process(new WindowResult())
                .print();


        env.execute("UserViewCountDemo");


    }

    private static class WindowResult extends ProcessWindowFunction<ClickSource.Click, UserViewCountPreWindow, String, TimeWindow> {

        @Override
        public void process(String key, Context ctx, Iterable<ClickSource.Click> elements, Collector<UserViewCountPreWindow> out) throws Exception {

            //iterable 窗口内全部的数据
            // elements.spliterator().getExactSizeIfKnown() 返回迭代器里面的数据的数量
            out.collect(new UserViewCountPreWindow(key,
                    elements.spliterator().getExactSizeIfKnown(),
                    ctx.window().getStart(),
                    ctx.window().getEnd()
            ));
        }
    }
}
