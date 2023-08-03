package com.flink.windows;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.ProcessingTimeSessionWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.sql.Timestamp;

/**
 * @Author wangziyu1
 * @Date 2022/11/14 15:17
 * @Version 1.0
 * <p>
 * 会话窗口的举例
 */
public class SessionProcessTimeWindowDemo {


    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);

        env.addSource(new SourceFunction<String>() {
                    @Override
                    public void run(SourceContext<String> ctx) throws Exception {
                        ctx.collect("a");
                        Thread.sleep(1000L);

                        ctx.collect("a");
                        Thread.sleep(10 * 1000L);

                        ctx.collect("a");
                    }

                    @Override
                    public void cancel() {

                    }
                })
                .keyBy(r -> true)
                .window(ProcessingTimeSessionWindows.withGap(Time.seconds(5)))
                .process(new ProcessWindowFunction<String, String, Boolean, TimeWindow>() {
                    @Override
                    public void process(Boolean key, Context ctx, Iterable<String> elements, Collector<String> out) throws Exception {

                        out.collect("key 是" + key + ",当前窗口的开始时间" + new Timestamp(ctx.window().getStart())+
                                "~" + new Timestamp(ctx.window().getEnd()) + "当前窗口一共有"
                                        + elements.spliterator().getExactSizeIfKnown());

                    }
                })
                .print();


        env.execute();


    }
}
