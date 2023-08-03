package com.flink.uv;

import com.flink.bean.UserBehavior;

import com.google.common.base.Charsets;
import org.apache.flink.shaded.guava18.com.google.common.hash.BloomFilter;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.shaded.guava18.com.google.common.hash.Funnels;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.sql.Timestamp;

/**
 * @Author wangziyu1
 * @Date 2022/11/15 19:29
 * @Version 1.0
 * ProduceUVDemo 的优化版
 * 布隆过滤器版本的uv
 */
public class ProduceBloomFilterUVDemo {

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

    //使用布隆过滤器
    //类型是个Tuple2<BloomFilter<String>,Long>
    private static class CountAgg implements AggregateFunction<UserBehavior, Tuple2<BloomFilter<Long>, Long>, Long> {

        @Override
        public Tuple2<BloomFilter<Long>, Long> createAccumulator() {
            return Tuple2.of(
                    BloomFilter.create(Funnels.longFunnel(), //待去重的字段是 Long 类型
                            10000,                                         //估算一下要去重的数据量
                            0.01),                                          //误判率
                    0L  //  统计值的初始值
            );
        }

        @Override
        public Tuple2<BloomFilter<Long>, Long> add(UserBehavior in, Tuple2<BloomFilter<Long>, Long> acc) {

            //如果in之前不在bloomFilter 那么一定没来过
            if (!acc.f0.mightContain(in.getUserId())) {
                //将bit位数组置位1
                acc.f0.put(in.getUserId());
                //累加器+1
                acc.f1 += 1L;
            }
            return acc;
        }

        @Override
        public Long getResult(Tuple2<BloomFilter<Long>, Long> acc) {
            return acc.f1;
        }

        @Override
        public Tuple2<BloomFilter<Long>, Long> merge(Tuple2<BloomFilter<Long>, Long> bloomFilterLongTuple2, Tuple2<BloomFilter<Long>, Long> acc1) {
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
