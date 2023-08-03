package com.flink.sql;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

/**
 * @Author wangziyu1
 * @Date 2022/12/6 22:25
 * @Version 1.0
 */
public class DDLDemo {

    public static void main(String[] args) {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();


        StreamTableEnvironment tabEnv = StreamTableEnvironment.create(env, EnvironmentSettings.newInstance().inStreamingMode().build());


        //定义输入表
        tabEnv.executeSql("create table clicks (`user` STRING, `url` STRING) " +
                "WITH (" +
                "'connector' = 'filesystem'," +
                " 'path' = '/Users/wangziyu/IdeaProjects/KnowledgeAccumulation/FlinkCDC/src/main/resources/file.csv'," +
                " 'format' = 'csv'" +
                ")");

        //定义输出表
        tabEnv.executeSql("create table ResultTable(`user` STRING,`cnt` BIGINT)" +
                " WITH(" +
                " 'connector' = 'print')");

        tabEnv.executeSql("insert into ResultTable select user,count(1) from clicks group by user");


    }


}
