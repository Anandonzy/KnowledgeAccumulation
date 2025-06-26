package com.flink.watermark;

import org.apache.flink.api.common.eventtime.*;
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
 * @Date 2022/11/10 16:02
 * @Version 1.0
 * 自定义水位线发生器.
 * 不用api提供的水位线发生器
 * 帮助我们理解水位线的原理.
 */
public class MyWaterMarkDemo4 {


    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);

        env.socketTextStream("localhost", 9999)
                .map(new MapFunction<String, Tuple2<String, Long>>() {
                    @Override
                    public Tuple2<String, Long> map(String in) throws Exception {
                        String[] values = in.split(" ");

                        return Tuple2.of(values[0], Long.parseLong(values[1]) * 1000L);
                    }
                })
                //设置最大延迟为5s
                .assignTimestampsAndWatermarks(new WatermarkStrategy<Tuple2<String, Long>>() {
                    @Override
                    public TimestampAssigner<Tuple2<String, Long>> createTimestampAssigner(TimestampAssignerSupplier.Context context) {
                        return new SerializableTimestampAssigner<Tuple2<String, Long>>() {
                            @Override
                            public long extractTimestamp(Tuple2<String, Long> element, long l) {
                                return element.f1;
                            }
                        };
                    }

                    //自定义watermark 生产规则
                    @Override
                    public WatermarkGenerator<Tuple2<String, Long>> createWatermarkGenerator(WatermarkGeneratorSupplier.Context context) {

                        return new WatermarkGenerator<Tuple2<String, Long>>() {


                            private final long delay = 5000L;
                            private long maxTs = -Long.MIN_VALUE + delay + 1L; //用来保存观察到的最大时间戳. 为了防止溢出需要加上+delay + 1ms

                            @Override
                            public void onEvent(Tuple2<String, Long> event, long l, WatermarkOutput output) {
                                //每来一条数据触发一次调用.
                                //更新观察都的最大时间戳 选择最大的时间戳
                                maxTs = Math.max(event.f1, maxTs);

                                //除了在 onPeriodicEmit 里面发送水位线 也可以在这里发送水位线
                                if (event.f0.equals("hello")) {

                                    //向下发送一个正无穷的水位线 关闭全部的窗口
                                    output.emitWatermark(new Watermark(Long.MAX_VALUE));
                                }

                            }

                            //周期性的向下游发送水位线
                            @Override
                            public void onPeriodicEmit(WatermarkOutput output) {
                                // onPeriodicEmit 默认每隔200ms触发一次 往keyBy 下游发送
                                output.emitWatermark(
                                        new Watermark(maxTs - delay - 1L)
                                );

                            }
                        };
                    }
                })
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
