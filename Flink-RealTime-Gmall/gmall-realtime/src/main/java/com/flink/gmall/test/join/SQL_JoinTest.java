package com.flink.gmall.test.join;

import com.flink.gmall.bean.WaterSensor;
import com.flink.gmall.bean.WaterSensor2;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import java.time.Duration;

/**
 * @Author wangziyu1
 * @Date 2022/8/18 17:32
 * @Version 1.0
 * FlinkSql Join 测试
 */
public class SQL_JoinTest {

    public static void main(String[] args) {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        //会打印 PT0S pressingTime 0s 默认关闭 不会过期
        System.out.println(tableEnv.getConfig().getIdleStateRetention());
        //设置数据过期时间 如果达到这个值之后就会过期了.
        tableEnv.getConfig().setIdleStateRetention(Duration.ofSeconds(10));

        //1001,23.6,1324
        SingleOutputStreamOperator<WaterSensor> waterSensorDS1 = env.socketTextStream("localhost", 8888)
                .map(line -> {
                    String[] split = line.split(",");
                    return new WaterSensor(split[0],
                            Double.parseDouble(split[1]),
                            Long.parseLong(split[2]));
                });
        SingleOutputStreamOperator<WaterSensor2> waterSensorDS2 = env.socketTextStream("localhost", 9999)
                .map(line -> {
                    String[] split = line.split(",");
                    return new WaterSensor2(split[0],
                            split[1],
                            Long.parseLong(split[2]));
                });

        //将流转换为动态表
        tableEnv.createTemporaryView("t1", waterSensorDS1);
        tableEnv.createTemporaryView("t2", waterSensorDS2);

        //FlinkSQLJOIN
        //inner join 左表:OnCreateAndWrite   右表:OnCreateAndWrite
        //tableEnv.sqlQuery("select t1.id,t1.vc,t2.id,t2.name from t1 join t2 on t1.id=t2.id")
        //        .execute()
        //        .print();


        //left join   左表:OnReadAndWrite     右表:OnCreateAndWrite
        //tableEnv.sqlQuery("select t1.id,t1.vc,t2.id,t2.name from t1 left join t2 on t1.id=t2.id")
        //        .execute()
        //        .print();

        ////right join  左表:OnCreateAndWrite   右表:OnReadAndWrite
        //tableEnv.sqlQuery("select t1.id,t1.vc,t2.id,t2.name from t1 right join t2 on t1.id=t2.id")
        //        .execute()
        //        .print();

        //full join   左表:OnReadAndWrite     右表:OnReadAndWrite
        //tableEnv.sqlQuery("select t1.id,t1.vc,t2.id,t2.name from t1 full join t2 on t1.id=t2.id")
        //        .execute()
        //        .print();


        Table result = tableEnv.sqlQuery("select t1.id,t2.name,t1.vc from t1 left join t2 on t1.id=t2.id");

        //普通的kafkaconnector
        //tableEnv.executeSql("create table t12(id string,name string,age double" +
        //        ")with(" +
        //        "'connector'='kafka'," +
        //        "'properties.bootstrap.servers'='192.168.200.144:9092'," +
        //        "'topic'='s12'," +
        //        "'format'='json'" +
        //        ")");

        //如果使用的是left join的方式 就要用upsert-kafka connector 更新数据的时候就会往kafka 发送一个null的数据.
        tableEnv.executeSql("create table t13(id string,name string,age double" +
                ",primary key(id) not enforced" +
                ")with(" +
                "'connector'='upsert-kafka'," +
                "'properties.bootstrap.servers'='192.168.200.144:9092'," +
                "'topic'='s13'," +
                "'key.format'='json'," +
                "'value.format'='json'" +
                ")");
        result.executeInsert("t13");

        //如果下游消费刚刚写入的数据 用普通的kafka消费就直接可以过滤null

    }

}
