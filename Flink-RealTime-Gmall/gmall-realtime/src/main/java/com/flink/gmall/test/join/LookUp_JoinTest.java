package com.flink.gmall.test.join;

import com.flink.gmall.bean.WaterSensor;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import static org.apache.flink.table.api.Expressions.$;

/**
 * @Author wangziyu1
 * @Date 2022/8/22 12:45
 * @Version 1.0
 * LookUp Join Test
 */
public class LookUp_JoinTest {

    public static void main(String[] args) {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
        //查询MySQL构建LookUp表
        tableEnv.executeSql("" +
                "create TEMPORARY table base_dic( " +
                "    `dic_code` String, " +
                "    `dic_name` String, " +
                "    `parent_code` String, " +
                "    `create_time` String, " +
                "    `operate_time` String " +
                ") WITH ( " +
                "  'connector' = 'jdbc', " +
                "  'url' = 'jdbc:mysql://localhost:3306/gmall', " +
                "  'table-name' = 'base_dic', " +
                "  'driver' = 'com.mysql.cj.jdbc.Driver', " +
                //"  'lookup.cache.max-rows' = '10', " +  //维表数据不变or会改变,但是数据的准确度要求不高
                //"  'lookup.cache.ttl' = '1 hour', " +
                "  'username' = 'root', " +
                "  'password' = '12345678' " +
                ")");

        //打印LookUp表
        tableEnv.sqlQuery("select * from base_dic")
                .execute()
                .print();

        //构建事实表
        SingleOutputStreamOperator<WaterSensor> waterSensorDS = env.socketTextStream("localhost", 8888)
                .map(line -> {
                    String[] split = line.split(",");
                    return new WaterSensor(split[0],
                            Double.parseDouble(split[1]),
                            Long.parseLong(split[2]));
                });
        Table table = tableEnv.fromDataStream(waterSensorDS,
                $("id"),
                $("vc"),
                $("ts"),
                $("pt").proctime()); //使用proctime 必须要写
        tableEnv.createTemporaryView("t1", table);


        //使用事实表关联维表并打印结果
        tableEnv.sqlQuery("" +
                        "select " +
                        "    t1.id, " +
                        "    t1.vc, " +
                        "    dic.dic_name " +
                        "from t1 " +
                        "join base_dic FOR SYSTEM_TIME AS OF t1.pt as dic " +
                        "on t1.id=dic.dic_code")
                .execute().print();

    }
}
