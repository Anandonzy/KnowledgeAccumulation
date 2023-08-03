package com.flink.watermark;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.sql.Timestamp;
import java.time.Duration;

/**
 * @Author wangziyu1
 * @Date 2022/11/11 10:54
 * @Version 1.0
 * watermark 水位线传播原理验证:
 * 分流：水位线复制然后广播到下游的所有并行子任务中去。
 * <p>
 * 合流：
 * 第一步：到达的水位线覆盖数组中对应的水位线。
 * 第二步：选择水位线数组中的最小的水位线来更新并行子任务的逻辑时钟。
 */
public class WaterMarkSpreadUnionDemo {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);

        SingleOutputStreamOperator<Tuple2<String, Long>> stream1 = env.socketTextStream("localhost", 9999)
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
                }));

        SingleOutputStreamOperator<Tuple2<String, Long>> stream2 = env.socketTextStream("localhost", 9998
                )
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
                }));

        //union 内部会维护一个队列 FIFO 先进先出
        stream1.union(stream2)
                .process(new ProcessFunction<Tuple2<String, Long>, String>() {
                    @Override
                    public void processElement(Tuple2<String, Long> in, Context ctx, Collector<String> out) throws Exception {

                        out.collect(
                                "输入的数据:" + in
                                        + ",当前的水位线是:" + ctx.timerService().currentWatermark()
                        );


                    }
                }).setParallelism(4)
                .print()
                .setParallelism(4);


        env.execute("WaterMarkSpreadUnionDemo");
    }

}
