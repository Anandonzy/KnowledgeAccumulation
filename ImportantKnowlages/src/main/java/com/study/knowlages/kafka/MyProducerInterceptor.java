package com.study.knowlages.kafka;

/**
 * @Author wangziyu1
 * @Date 2022/4/21 14:13
 * @Version 1.0
 * 自定义拦截器
 */
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Map;
// 自定义拦截器
public class MyProducerInterceptor implements ProducerInterceptor {
    @Override
    public ProducerRecord onSend(ProducerRecord oldRecord) {
        ProducerRecord newRecord = new ProducerRecord(oldRecord.topic(), oldRecord.key(), "test:" + oldRecord.value());
        return newRecord;
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {

    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> configs) {

    }
}
