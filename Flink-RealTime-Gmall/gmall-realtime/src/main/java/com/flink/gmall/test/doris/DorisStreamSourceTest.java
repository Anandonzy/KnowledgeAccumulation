package com.flink.gmall.test.doris;

import org.apache.doris.flink.cfg.DorisStreamOptions;
import org.apache.doris.flink.datastream.DorisSourceFunction;
import org.apache.doris.flink.deserialization.SimpleListDeserializationSchema;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Properties;

/**
 * @Author wangziyu1
 * @Date 2022/9/19 10:38
 * @Version 1.0
 */
public class DorisStreamSourceTest {


    public static void main(String[] args) throws Exception {

        //TODO 1.获取执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1); //生产环境中设置为Kafka主题的分区数

        Properties pro = new Properties();
        pro.setProperty("fenodes", "192.168.15.205:7030");
        pro.setProperty("username", "root");
        pro.setProperty("password", "aaaaaa");
        pro.setProperty("table.identifier", "test_db.table1");

        env.addSource(new DorisSourceFunction(new DorisStreamOptions(pro), new SimpleListDeserializationSchema()))
                .print();

        env.execute();

    }
}
