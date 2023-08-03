package com.study.pv;

import com.flink.bean.UserBehavior;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

/**
 * @Author wangziyu1
 * @Date 2023/2/6 16:16
 * @Version 1.0
 */
public class PvDemo {

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
                .process(new KeyedProcessFunction<Long, UserBehavior, Long>() {
                    private long pvCount = 0;

                    @Override
                    public void processElement(UserBehavior in, Context ctx, Collector<Long> out) throws Exception {
                        pvCount++;
                        out.collect(pvCount);
                    }
                })
                .print();


        env.execute();

    }
}
