package com.flink.ads;


import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.util.Collector;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class Test {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(10);
        DataStream<String> ds =  env.fromElements("111","112","113","111");
        ds.keyBy(new KeySelector<String, String>() {
            @Override
            public String getKey(String value) throws Exception {
                String shard = "shard_" + value;
                return shard;
            }
        }).process(new KeyedProcessFunction<String, String, Tuple2<String, String>>() {

            @Override
            public void processElement(String value, Context ctx, Collector<Tuple2<String, String>> out) throws Exception {
                String shard_key = ctx.getCurrentKey();
                out.collect(Tuple2.of(shard_key, value));
            }

        }).addSink(new SinkFunction<Tuple2<String, String>>() {
            // shardKey, ck连接
            private final Map<String, Connection> connectionMap = new HashMap<>();

            public void invoke(Tuple2<String, String> value, Context context) throws Exception {
                Connection connection = connectionMap.get(value.f0);

                // 保存数据
                System.out.println(this + "==============" + value);
            }
        });

        env.execute();
    }

}
