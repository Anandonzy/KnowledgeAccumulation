package com.flink.cdc;

import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

/**
 * @version 1.0
 * @Author wangziyu1
 * @Date 9/9/21 7:21 PM
 * @注释 FlinkCDC sql的方式实现
 */
public class FlinkcdcSql {


    public static void main(String[] args) {

        //1.创建执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        //2.创建Flink-MySQL-CDC的Source
        tableEnv.executeSql("CREATE TABLE aa (" +
                "  name STRING," +
                " age INT" +
                ") WITH (" +
                "  'connector' = 'mysql-cdc'," +
                "  'hostname' = '192.168.200.144'," +
                "  'port' = '3306'," +
                "  'username' = 'root'," +
                "  'password' = 'S47OJ6VvwCndcAm.JY'," +
                "  'database-name' = 'test'," +
                "  'table-name' = 'aa'" +
                ")");

        tableEnv.executeSql("select * from aa").print();

        try {
            env.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
