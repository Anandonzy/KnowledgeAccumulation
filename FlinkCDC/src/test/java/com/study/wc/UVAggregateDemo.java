package com.study.wc;

import com.alibaba.fastjson.JSONObject;
import com.flink.api.soruce.ClickSource;
import com.flink.windows.UserViewCountPreWindow;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.time.Duration;

/**
 * @Author wangziyu1
 * @Date 2023/2/7 14:20
 * @Version 1.0
 */
public class UVAggregateDemo {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);


        env.addSource(new ClickSource())
                .assignTimestampsAndWatermarks(WatermarkStrategy.<ClickSource.Click>forBoundedOutOfOrderness(Duration.ofSeconds(2)).withTimestampAssigner(new SerializableTimestampAssigner<ClickSource.Click>() {
                    @Override
                    public long extractTimestamp(ClickSource.Click element, long recordTimestamp) {
                        return element.getTs();
                    }
                }))
                .keyBy(ClickSource.Click::getName)
                .window(TumblingEventTimeWindows.of(Time.seconds(10)))
                .aggregate(new AggregateFunction<ClickSource.Click, Integer, Integer>() {
                    @Override
                    public Integer createAccumulator() {
                        return 0;
                    }

                    @Override
                    public Integer add(ClickSource.Click in, Integer acc) {
                        return acc + 1;
                    }

                    @Override
                    public Integer getResult(Integer acc) {
                        return acc;
                    }

                    @Override
                    public Integer merge(Integer a, Integer b) {
                        return null;
                    }
                }, new ProcessWindowFunction<Integer, UserViewCountPreWindow, String, TimeWindow>() {
                    @Override
                    public void process(String key, Context ctx, Iterable<Integer> elements, Collector<UserViewCountPreWindow> out) throws Exception {


                        out.collect(new UserViewCountPreWindow(
                                key,
                                elements.iterator().next(),
                                ctx.window().getStart(),
                                ctx.window().getEnd()
                        ));
                    }
                })
                .print();


        env.execute();


    }
}
