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
 * @Date 2022/9/27 14:40
 * @Version 1.0
 */
public class WordCount2 {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();


        DataStreamSource<String> streamSource = env.fromElements("hello world", "hello world");

        //hello world --> (hello,1) (world,1)
        SingleOutputStreamOperator<WorldCount> fl = streamSource.flatMap(new FlatMapFunction<String, WorldCount>() {

            @Override
            public void flatMap(String value, Collector<WorldCount> collector) throws Exception {
                String[] words = value.split(" ");

                for (String word : words) {
                    collector.collect(new WorldCount(word, 1));
                }
            }
        }).setParallelism(1);

        fl.keyBy(r -> r.getWords())

                //reduce 会维护一个hash表 然后根据key去维护一个累加器 第一次来的时候会创建一个累加器 后续直接使用累加器即可.
                .reduce(new ReduceFunction<WorldCount>() {
                    @Override
                    public WorldCount reduce(WorldCount in, WorldCount accumulator) throws Exception {

                        return new WorldCount(in.getWords(), in.getCount() + accumulator.getCount());
                    }
                }).setParallelism(1)
                //.sum(1) //reduce 累加计算的和 sum是等价的
                .print().setParallelism(1);

        env.execute();
    }

}

class WorldCount {
    private String words;
    private Integer count;

    public String getWords() {
        return words;
    }

    public void setWords(String words) {
        this.words = words;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public WorldCount() {
    }

    public WorldCount(String words, Integer count) {
        this.words = words;
        this.count = count;
    }

    @Override
    public String toString() {
        return "WorldCount{" +
                "words='" + words + '\'' +
                ", count=" + count +
                '}';
    }
}
