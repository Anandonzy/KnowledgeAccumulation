package com.flink.api.sink;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;

/**
 * @Author wangziyu1
 * @Date 2022/11/14 10:36
 * @Version 1.0
 */
public class Sink_Kafka_Demo2 {


    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);

        env.readTextFile("/Users/wangziyu/Desktop/UserBehavior.csv")
                .addSink(new FlinkKafkaProducer<String>("192.168.200.144:9092", "topic_sensor", new SimpleStringSchema()));


        env.execute();


    }
}
