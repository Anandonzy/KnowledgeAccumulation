package com.study.knowlages.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * @Author wangziyu1
 * @Date 2022/4/21 14:08
 * @Version 1.0
 */
public class Kafka02_Test_Producer_Transaction {

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

        props.put("interceptor.classes", "com.atguigu.itdachang.kafka.MyProducerInterceptor");
        props.put("enable.idempotence", "true");
        props.put("transactional.id", "test");
        // 构建生产者对象
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        // TODO 事务操作必须设定事务ID
        //      事务操作时如果失败，那么数据是无法保存的，但是会将事务操作保存到日志中

        // 增加事务操作
        // 初始化事务
        producer.initTransactions();

        try {
            // 开启事务
            producer.beginTransaction();
            int i = 10;
            producer.send(new ProducerRecord<String, String>("itdachang-1", Integer.toString(i), Integer.toString(i)));
            int j = i / 0;
            i = 11;
            producer.send(new ProducerRecord<String, String>("itdachang-1", Integer.toString(i), Integer.toString(i)));
            // 提交事务
            producer.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            // 回滚事务
            producer.abortTransaction();
        }

        // 关闭生产者对象
        producer.close();
    }
}

