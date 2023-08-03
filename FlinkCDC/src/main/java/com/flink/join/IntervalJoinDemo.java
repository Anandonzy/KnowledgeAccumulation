package com.flink.join;

import lombok.*;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.ProcessJoinFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

/**
 * @Author wangziyu1
 * @Date 2022/11/15 11:38
 * @Version 1.0
 * leftStream 和 rightStream 两条流join
 * 第一条流 join 第二条流的[-5,5] 的所有数据.
 */
public class IntervalJoinDemo {


    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);

        SingleOutputStreamOperator<Event> leftStream = env.fromElements(new Event("key-01", "left", 10 * 1000L))
                .assignTimestampsAndWatermarks(WatermarkStrategy.<Event>forMonotonousTimestamps().withTimestampAssigner(new SerializableTimestampAssigner<Event>() {
                    @Override
                    public long extractTimestamp(Event event, long l) {
                        return event.getTs();
                    }
                }));

        SingleOutputStreamOperator<Event> rightStream = env.fromElements(
                        new Event("key-01", "right", 1000L),
                        new Event("key-01", "right", 6000L),
                        new Event("key-01", "right", 11 * 1000L),
                        new Event("key-01", "right", 16 * 1000L))

                .assignTimestampsAndWatermarks(WatermarkStrategy.<Event>forMonotonousTimestamps().withTimestampAssigner(new SerializableTimestampAssigner<Event>() {
                    @Override
                    public long extractTimestamp(Event event, long l) {
                        return event.getTs();
                    }
                }));

        leftStream.keyBy(Event::getKey)
                .intervalJoin(rightStream.keyBy(Event::getKey))
                .between(Time.seconds(-5), Time.seconds(5)) //第一条流的每一个事件都会join 第二条流的过去五秒和将来的5s [-5,5]
                .process(new ProcessJoinFunction<Event, Event, String>() {
                    @Override
                    public void processElement(Event left, Event right, Context ctx, Collector<String> out) throws Exception {
                        out.collect(left + "->" + right);
                    }
                })
                .print();


        env.execute();
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class Event {

        public String key;
        public String value;
        public Long ts;
    }

}
