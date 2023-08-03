package com.flink.watermark;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.time.Duration;

/**
 * @Author wangziyu1
 * @Date 2022/11/10 15:57
 * @Version 1.0
 * 证明watermark 是200ms插入一次
 * 设置watermark 1min插入一次.
 * 依次输入:
 * a 1
 * a 2
 * a 3
 * a 15
 * a 1
 * a 2
 * a 3
 *最终输出
 * number在窗口0~10000一共有:6
 * 再次证明了watermark 的插入是200ms 如果很长 最开始水位线就是 int最小值
 */
public class WaterMarkDemo3 {


    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);
        env.getConfig().setAutoWatermarkInterval(60 * 1000L);

        env.socketTextStream("localhost", 9999)
                .map(new MapFunction<String, Tuple2<String, Long>>() {
                    @Override
                    public Tuple2<String, Long> map(String in) throws Exception {
                        String[] values = in.split(" ");

                        return Tuple2.of(values[0], Long.parseLong(values[1]) * 1000L);
                    }
                })
                //设置最大延迟为5s
                .assignTimestampsAndWatermarks(WatermarkStrategy.<Tuple2<String, Long>>forBoundedOutOfOrderness(Duration.ofSeconds(5)).withTimestampAssigner(new SerializableTimestampAssigner<Tuple2<String, Long>>() {
                    @Override
                    public long extractTimestamp(Tuple2<String, Long> in, long l) {
                        return in.f1; //指定哪个一个字段是事件时间.
                    }
                }))
                .keyBy(r -> "number")
                .window(TumblingEventTimeWindows.of(Time.seconds(10)))
                .process(new ProcessWindowFunction<Tuple2<String, Long>, String, String, TimeWindow>() {
                    @Override
                    public void process(String key, Context ctx, Iterable<Tuple2<String, Long>> elements, Collector<String> out) throws Exception {

                        out.collect(key + "在窗口"
                                + ctx.window().getStart() + "~" + ctx.window().getEnd()
                                + "一共有:" + elements.spliterator().getExactSizeIfKnown());
                    }
                })
                .print();


        env.execute();


    }
}
