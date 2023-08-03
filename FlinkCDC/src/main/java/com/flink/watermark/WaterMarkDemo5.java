package com.flink.watermark;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

/**
 * @Author wangziyu1
 * @Date 2022/11/10 16:23
 * @Version 1.0
 * <p>
 * 自定义数据源里面发送水位线
 * 再次证明run方法之前发送-max的水位线,
 * 在run方法之后发送max的水位线.
 */
public class WaterMarkDemo5 {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);

        env.addSource(new SourceFunction<Integer>() {
                    @Override
                    public void run(SourceContext<Integer> ctx) throws Exception {
                        //会在run方法执行之前发送一个-max的水位线
                        //会在run方法执行之后发送一个max 的水位线
                        ctx.emitWatermark(new Watermark(5000L));
                        ctx.collectWithTimestamp(1, 6000L);
                        ctx.emitWatermark(new Watermark(10 * 1000L));
                    }

                    @Override
                    public void cancel() {

                    }
                })
                .keyBy(r -> "number")
                .window(TumblingEventTimeWindows.of(Time.seconds(5)))
                .process(new ProcessWindowFunction<Integer, String, String, TimeWindow>() {
                    @Override
                    public void process(String key, Context ctx, Iterable<Integer> elements, Collector<String> out) throws Exception {

                        out.collect(key + "在窗口"
                                + ctx.window().getStart() + "~" + ctx.window().getEnd()
                                + "一共有:" + elements.spliterator().getExactSizeIfKnown());
                    }
                })
                .print();

        env.execute();


    }
}
