package com.flink.sideoutput;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.time.Duration;

/**
 * @Author wangziyu1
 * @Date 2022/11/14 15:06
 * @Version 1.0
 * 允许迟到数据 再一次触发窗口计算
 * .allowedLateness(Time.seconds(5))：窗口会等待5秒钟的迟到事件
 */
public class LateSideOutputWindowAllowedLatenessDemo {

    public static void main(String[] args) throws Exception {


        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);

        SingleOutputStreamOperator<String> result = env.socketTextStream("localhost", 9999)
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
                .keyBy(r -> r.f0)
                .window(TumblingEventTimeWindows.of(Time.seconds(10)))
                //这个地方是允许数据迟到5s 仍然触发上个窗口的计算.
                .allowedLateness(Time.seconds(5))
                //将迟到且不存在的数据输出到测输出流里面
                .sideOutputLateData(new OutputTag<Tuple2<String, Long>>("lateOut") {
                })
                .process(new ProcessWindowFunction<Tuple2<String, Long>, String, String, TimeWindow>() {
                    @Override
                    public void process(String key, Context ctx, Iterable<Tuple2<String, Long>> elements, Collector<String> out) throws Exception {

                        out.collect(key + "在窗口"
                                + ctx.window().getStart() + "~" + ctx.window().getEnd()
                                + "一共有:" + elements.spliterator().getExactSizeIfKnown());
                    }
                });

        result.print("主流:");
        result.getSideOutput(new OutputTag<Tuple2<String, Long>>("lateOut") {
        }).print("测流:");

        env.execute();


    }
}
