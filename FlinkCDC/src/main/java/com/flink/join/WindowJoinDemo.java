package com.flink.join;

import lombok.*;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.ProcessJoinFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

/**
 * @Author wangziyu1
 * @Date 2022/11/15 14:31
 * @Version 1.0
 * 基于窗口的window的join
 */
public class WindowJoinDemo {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);

        SingleOutputStreamOperator<Event> leftStream = env.fromElements(
                        new Event("key-01", "left", 7 * 1000L),
                        new Event("key-01", "left", 13 * 1000L)
                )
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

        //窗口的join 指定key 和 传统的join不一样 不用直接keyby
        leftStream.join(rightStream)
                .where(Event::getKey)
                .equalTo(Event::getKey)
                .window(TumblingEventTimeWindows.of(Time.seconds(10)))
                .apply(new JoinFunction<Event, Event, String>() { //基于窗口的join 使用apply方法
                    @Override
                    public String join(Event left, Event right) throws Exception {
                        return left + "->" + right;
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
