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

import java.sql.Timestamp;
import java.time.Duration;

/**
 * @Author wangziyu1
 * @Date 2022/11/11 10:32
 * @Version 1.0
 * watermark 水位线传播原理验证:
 * 分流：水位线复制然后广播到下游的所有并行子任务中去。
 *
 * 合流：
 * 第一步：到达的水位线覆盖数组中对应的水位线。
 * 第二步：选择水位线数组中的最小的水位线来更新并行子任务的逻辑时钟。
 */
public class WaterMarkSpreadDemo {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);

        env.socketTextStream("localhost", 9999)
                .map(new MapFunction<String, Tuple2<String, Long>>() {
                    @Override
                    public Tuple2<String, Long> map(String in) throws Exception {
                        String[] value = in.split(" ");
                        return Tuple2.of(value[0], Long.parseLong(value[1]) * 1000L);
                    }
                }).setParallelism(1)
                .assignTimestampsAndWatermarks(WatermarkStrategy.<Tuple2<String, Long>>forBoundedOutOfOrderness(Duration.ofSeconds(0)).withTimestampAssigner(new SerializableTimestampAssigner<Tuple2<String, Long>>() {
                    @Override
                    public long extractTimestamp(Tuple2<String, Long> in, long l) {
                        return in.f1;
                    }
                }))
                .keyBy(r -> r.f0)
                .window(TumblingEventTimeWindows.of(Time.seconds(5)))
                .process(new ProcessWindowFunction<Tuple2<String, Long>, String, String, TimeWindow>() {

                    //每一个并行子任务一个水位线 跟key没关系的
                    @Override
                    public void process(String key, Context ctx, Iterable<Tuple2<String, Long>> elements, Collector<String> out) throws Exception {
                        out.collect(
                                "当前key:" + key +
                                        "窗口的开始时间是:" + new Timestamp(ctx.window().getStart())
                                        + " 统计的数量是:" + elements.spliterator().getExactSizeIfKnown()
                                        + ", 并行子任务的索引是:" + getRuntimeContext().getIndexOfThisSubtask());

                    }
                }).setParallelism(4)
                .print()
                .setParallelism(4);


        env.execute("WaterMarkSpreadDemo");
    }


}
