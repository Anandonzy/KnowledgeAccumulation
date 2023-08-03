package com.flink.state;

import com.flink.api.soruce.ClickSource;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

/**
 * @Author wangziyu1
 * @Date 2022/11/7 17:00
 * @Version 1.0
 */
public class Flink03_State_Map_State_ByReduce_Count {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.addSource(new ClickSource())
                .map(new MapFunction<ClickSource.Click, Tuple2<Tuple2<String, String>, Integer>>() {
                    @Override
                    public Tuple2<Tuple2<String, String>, Integer> map(ClickSource.Click click) throws Exception {
                        return Tuple2.of(Tuple2.of(click.getName(), click.getUrl())
                                , 1);
                    }
                })
                .keyBy(new KeySelector<Tuple2<Tuple2<String, String>, Integer>, Tuple2<String, String>>() {
                    @Override
                    public Tuple2<String, String> getKey(Tuple2<Tuple2<String, String>, Integer> value) throws Exception {
                        return value.f0;
                    }
                })
                .reduce(new ReduceFunction<Tuple2<Tuple2<String, String>, Integer>>() {

                    @Override
                    public Tuple2<Tuple2<String, String>, Integer> reduce(Tuple2<Tuple2<String, String>, Integer> v1, Tuple2<Tuple2<String, String>, Integer> v2) throws Exception {
                        return Tuple2.of(v1.f0, v1.f1 + v2.f1);
                    }
                })
                .print();

        env.execute();

    }


    private static class ClickProcess extends KeyedProcessFunction<String, Tuple2<String, String>, String> {

        @Override
        public void processElement(Tuple2<String, String> stringStringTuple2, Context context, Collector<String> collector) throws Exception {

        }
    }
}
