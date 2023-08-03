package com.flink.gmall.test.doris;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

/**
 * @Author wangziyu1
 * @Date 2022/9/19 15:11
 * @Version 1.0
 */
public class DorisStreamSinkSqlTest {

    public static void main(String[] args) throws Exception {

        //TODO 1.获取执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1); //生产环境中设置为Kafka主题的分区数
        StreamTableEnvironment tEnv = StreamTableEnvironment.create(env);


        tEnv.executeSql("CREATE TABLE flink_doris (  " +
                "    siteid INT,  " +
                "    citycode SMALLINT,  " +
                "    username STRING,  " +
                "    pv BIGINT  " +
                "    )   " +
                "    WITH (  " +
                "      'connector' = 'doris',  " +
                "      'fenodes' = '192.168.15.205:7030',  " +
                "      'table.identifier' = 'test_db.table1',  " +
                "      'username' = 'root',  " +
                "      'password' = 'aaaaaa'  " +
                ")  ");
        // 读
        tEnv.sqlQuery("select * from flink_doris").execute().print();


        //写
        tEnv.executeSql("insert into flink_doris(siteid,username,pv) values(3000,'wuyanzu',3)");


        //env.execute();


    }
}
