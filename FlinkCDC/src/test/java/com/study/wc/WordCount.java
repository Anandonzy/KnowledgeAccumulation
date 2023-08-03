package com.study.wc;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * @Author wangziyu1
 * @Date 2022/9/27 10:46
 * @Version 1.0
 */
public class WordCount {


    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();


        DataStreamSource<String> streamSource = env.socketTextStream("localhost", 9999);

        //hello world --> (hello,1) (world,1)
        SingleOutputStreamOperator<Tuple2<String, Integer>> fl = streamSource.flatMap(new FlatMapFunction<String, Tuple2<String, Integer>>() {

            @Override
            public void flatMap(String value, Collector<Tuple2<String, Integer>> collector) throws Exception {
                String[] words = value.split(" ");

                for (String word : words) {
                    collector.collect(Tuple2.of(word, 1));
                }
            }
        }).setParallelism(1);

        fl.keyBy(r -> r.f0)

                //reduce 会维护一个hash表 然后根据key去维护一个累加器 第一次来的时候会创建一个累加器 后续直接使用累加器即可.
                .reduce(new ReduceFunction<Tuple2<String, Integer>>() {
                    @Override
                    public Tuple2<String, Integer> reduce(Tuple2<String, Integer> in, Tuple2<String, Integer> accumulator) throws Exception {

                        return Tuple2.of(in.f0, in.f1 + accumulator.f1);
                    }
                }).setParallelism(1)
                //.sum(1) //reduce 累加计算的和 sum是等价的
                .print();

        env.execute();


    }
}
