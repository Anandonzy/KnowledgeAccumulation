package com.flink.state;


import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
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
 * @Date 2022/11/18 11:37
 * @Version 1.0
 * 演示 processWindowsFunction里面的窗口状态
 * 状态的作用域是窗口内
 * 统计窗口第一次触发
 *
 * - 窗口状态（Windowed State）：作用域是每个窗口。
 *     - 在Trigger中使用
 *     - 可以在ProcessWindowFunction中使用 作用域就是整个窗口了
 */
public class State_Value_State_Window_First {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        SingleOutputStreamOperator<String> result = env
                // a 1
                // a 2
                .socketTextStream("localhost", 9999)
                // "a 1" -> ("a", 1000L)
                .map(new MapFunction<String, Tuple2<String, Long>>() {
                    @Override
                    public Tuple2<String, Long> map(String in) throws Exception {
                        String[] array = in.split(" ");
                        return Tuple2.of(
                                array[0],
                                Long.parseLong(array[1]) * 1000L
                        );
                    }
                })
                // 在map算子输出的数据流中插入水位线事件
                // 默认每隔200ms插入一次水位线事件
                .assignTimestampsAndWatermarks(
                        // 设置最大延迟时间为5秒钟:`Duration.ofSeconds(5)`
                        WatermarkStrategy.<Tuple2<String, Long>>forBoundedOutOfOrderness(Duration.ofSeconds(5))
                                .withTimestampAssigner(new SerializableTimestampAssigner<Tuple2<String, Long>>() {
                                    @Override
                                    public long extractTimestamp(Tuple2<String, Long> element, long recordTimestamp) {
                                        return element.f1; // 指定哪一个字段是事件时间戳
                                    }
                                })
                )
                .keyBy(r -> r.f0)
                .window(TumblingEventTimeWindows.of(Time.seconds(10)))
                // 窗口会等待迟到数据5秒钟
                .allowedLateness(Time.seconds(5))
                // 将迟到且所属窗口已经不存在的数据发送到侧输出流中
                .sideOutputLateData(new OutputTag<Tuple2<String, Long>>("late-event") {
                })
                .process(new ProcessWindowFunction<Tuple2<String, Long>, String, String, TimeWindow>() {

                    @Override
                    public void process(String key, Context ctx, Iterable<Tuple2<String, Long>> elements, Collector<String> out) throws Exception {
                        // 初始化一个窗口状态变量
                        // 标志了窗口是否是第一次触发
                        ValueState<Boolean> flagState = ctx.windowState().getState(new ValueStateDescriptor<Boolean>("flag", Types.BOOLEAN));

                        if (flagState.value() == null) {
                            out.collect("水位线超过窗口的结束时间,窗口第一次触发."
                                    + "key :" + key + ",在窗口 "
                                    + ctx.window().getStart() + " ~ "
                                    + ctx.window().getEnd()
                                    + "里面有"
                                    + elements.spliterator().getExactSizeIfKnown() + "条数据");
                            flagState.update(true);
                        } else {
                            out.collect("迟到数据到达，更新窗口计算结果。" +
                                    "key: " + key + ", 在窗口" +
                                    "" + ctx.window().getStart() + "~" +
                                    "" + ctx.window().getEnd() + "里面有 " +
                                    "" + elements.spliterator().getExactSizeIfKnown() + " 条数据。");
                        }
                    }
                });

        result.print("主流");

        result.getSideOutput(new OutputTag<Tuple2<String, Long>>("late-event") {
        }).print("侧输出流");

        env.execute();
    }


}
