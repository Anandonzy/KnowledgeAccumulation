package com.flink.uv;

import com.flink.bean.UserBehavior;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.sql.Timestamp;
import java.util.HashSet;

/**
 * @Author wangziyu1
 * @Date 2022/11/15 17:37
 * @Version 1.0
 * 独立访客数 使用hashSet比较吃内存 不好
 * 建议使用BoolFliter
 */
public class ProduceUVDemo {


    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);

        env.readTextFile("/Users/wangziyu/Desktop/UserBehavior.csv")
                .map(new MapFunction<String, UserBehavior>() {
                    @Override
                    public UserBehavior map(String in) throws Exception {
                        String[] value = in.split(",");

                        return new UserBehavior(Long.parseLong(value[0]), Long.parseLong(value[1])
                                , Integer.parseInt(value[2]), value[3], Long.parseLong(value[4]) * 1000L);
                    }
                })
                //过滤浏览数据
                .filter(r -> "pv".equals(r.getBehavior()))
                .assignTimestampsAndWatermarks(WatermarkStrategy.<UserBehavior>forMonotonousTimestamps().withTimestampAssigner(new SerializableTimestampAssigner<UserBehavior>() {
                    @Override
                    public long extractTimestamp(UserBehavior in, long l) {
                        return in.getTimestamp();
                    }
                }))
                .keyBy(r -> "user") //分到一个流里面
                .window(TumblingEventTimeWindows.of(Time.hours(1)))
                .aggregate(new CountAgg(), new WindowResult())
                .print();


        env.execute();


    }

    private static class CountAgg implements AggregateFunction<UserBehavior, HashSet<Long>, Long> {

        @Override
        public HashSet<Long> createAccumulator() {
            return new HashSet<>();
        }

        @Override
        public HashSet<Long> add(UserBehavior in, HashSet<Long> acc) {
            //这里使用hashSet 去重
            acc.add(in.getUserId());
            return acc;
        }

        @Override
        public Long getResult(HashSet<Long> acc) {
            return (long) acc.size();
        }

        @Override
        public HashSet<Long> merge(HashSet<Long> longs, HashSet<Long> acc1) {
            return null;
        }
    }

    private static class WindowResult extends ProcessWindowFunction<Long, String, String, TimeWindow> {

        @Override
        public void process(String key, Context ctx, Iterable<Long> element, Collector<String> out) throws Exception {
            out.collect("窗口开始时间:" + new Timestamp(ctx.window().getStart()) +
                    " ~ " + new Timestamp(ctx.window().getEnd()) +
                    "访客数:" + element.iterator().next());
        }
    }
}
