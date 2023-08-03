package com.study.knowlages.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Map;
import java.util.Properties;

/**
 * @Author wangziyu1
 * @Date 2022/4/21 11:14
 * @Version 1.0
 */
public class Kafka01_Test_Producer {
    public static void main(String[] args) {
        // 创建配置对象，增加常规配置
        Properties props = new Properties();
        // 配置集群地址
        props.put("bootstrap.servers", "linux1:9092");
        // ACK应答
        props.put("acks", "all");
        // 重试次数
        props.put("retries", 1);
        // 批次大小 16K
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);//等待时间
        // 缓冲区内存大小 32M
        props.put("buffer.memory", 33554432);//RecordAccumulator缓冲区大小
        // Kafka发送数据时以k-v键值对方式发送
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        props.put("interceptor.classes", "com.study.knowlages.kafka.MyProducerInterceptor");
        props.put("enable.idempotence", "true");
        // 构建生产者对象
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        for (int i = 0; i < 2; i++) {
            // ProducerRecord生产模型
            // send方法用于发送数据
            producer.send(new ProducerRecord<String, String>("test", Integer.toString(i), Integer.toString(i)));
        }
        // 关闭生产者对象
        producer.close();
    }

}
