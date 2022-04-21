package com.flink.api.sink;

import com.alibaba.fastjson.JSON;
import com.flink.bean.WaterSensor;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.streaming.connectors.redis.RedisSink;
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommand;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommandDescription;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisMapper;

import java.util.ArrayList;


/**
 * @version 1.0
 * @Author wangziyu1
 * @Date 9/17/21 4:34 PM
 * @注释
 */
public class Flink01_Sink_Redis {

    public static void main(String[] args) throws Exception {
        ArrayList<WaterSensor> waterSensors = new ArrayList<>();
        waterSensors.add(new WaterSensor("sensor_1", 1607527992000L, 20));
        waterSensors.add(new WaterSensor("sensor_1", 1607527994000L, 50));
        waterSensors.add(new WaterSensor("sensor_1", 1607527996000L, 50));
        waterSensors.add(new WaterSensor("sensor_2", 1607527993000L, 10));
        waterSensors.add(new WaterSensor("sensor_2", 1607527995000L, 30));

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment().setParallelism(1);

        //链接到Redis的配置
        FlinkJedisPoolConfig redisConfig = new FlinkJedisPoolConfig.Builder()
                .setHost("192.168.200.144")
                .setPort(7001)
                .setPassword("S47OJ6VvwCndcAmJY")
                .setMaxTotal(100)
                .setTimeout(1000 * 10)
                .build();

        env
                .fromCollection(waterSensors)
                .addSink(new RedisSink<>(redisConfig, new RedisMapper<WaterSensor>() {

                       /*
                key                 value(hash)
                "sensor"            field           value
                                    sensor_1        {"id":"sensor_1","ts":1607527992000,"vc":20}
                                    ...             ...
               */

                    @Override
                    public RedisCommandDescription getCommandDescription() {
                        return new RedisCommandDescription(RedisCommand.HSET, "sensor");
                    }

                    @Override
                    public String getKeyFromData(WaterSensor waterSensor) {
                        return waterSensor.getId();
                    }

                    @Override
                    public String getValueFromData(WaterSensor waterSensor) {
                        return JSON.toJSONString(waterSensor);
                    }
                }));

        env.execute();
    }

}
