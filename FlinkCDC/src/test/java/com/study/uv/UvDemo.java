package com.study.uv;


import com.flink.bean.UserBehavior;
import org.apache.calcite.linq4j.tree.Types;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

import java.util.HashSet;
import java.util.Set;

/**
 * @Author wangziyu1
 * @Date 2023/2/6 16:44
 * @Version 1.0
 */
public class UvDemo {

    public static void main(String[] args) throws Exception {


        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);


        DataStreamSource<String> source = env.readTextFile("/Users/wangziyu/Desktop/UserBehavior.csv");
        source.flatMap(new FlatMapFunction<String, UserBehavior>() {
                    @Override
                    public void flatMap(String in, Collector<UserBehavior> out) throws Exception {

                        String[] split = in.split(",");
                        UserBehavior userBehavior = new UserBehavior(Long.valueOf(split[0]), Long.valueOf(split[1]), Integer.valueOf(split[2]), split[3], Long.valueOf(split[4]));
                        if ("pv".equals(userBehavior.getBehavior())) {
                            out.collect(userBehavior);
                        }
                    }
                })
                .keyBy(UserBehavior::getUserId)
                .map(new MapFunction<UserBehavior, Tuple2<String, Long>>() {
                    @Override
                    public Tuple2<String, Long> map(UserBehavior value) throws Exception {
                        return Tuple2.of("uv", value.getUserId());
                    }
                })
                .process(new ProcessFunction<Tuple2<String, Long>, Integer>() {
                    private Set<Long> set = new HashSet();

                    @Override
                    public void processElement(Tuple2<String, Long> in, Context ctx, Collector<Integer> out) throws Exception {
                        set.add(in.f1);
                        out.collect(set.size());
                    }
                }).print();
        env.execute();


    }
}
