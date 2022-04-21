package com.flink.api.sink;

import com.alibaba.fastjson.JSON;
import com.flink.bean.WaterSensor;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;

import java.util.ArrayList;


/**
 * @version 1.0
 * @Author wangziyu1
 * @Date 9/17/21 4:34 PM
 * @注释
 */
public class Flink01_Sink_Kafka {

    public static void main(String[] args) throws Exception {
        ArrayList<WaterSensor> waterSensors = new ArrayList<>();
        waterSensors.add(new WaterSensor("sensor_1", 1607527992000L, 20));
        waterSensors.add(new WaterSensor("sensor_1", 1607527994000L, 50));
        waterSensors.add(new WaterSensor("sensor_1", 1607527996000L, 50));
        waterSensors.add(new WaterSensor("sensor_2", 1607527993000L, 10));
        waterSensors.add(new WaterSensor("sensor_2", 1607527995000L, 30));

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment().setParallelism(1);
        env
                .fromCollection(waterSensors)
                .map(JSON::toJSONString)
                .addSink(new FlinkKafkaProducer<String>("192.168.200.144:9092", "topic_sensor", new SimpleStringSchema()));


        env.execute();
    }

}
