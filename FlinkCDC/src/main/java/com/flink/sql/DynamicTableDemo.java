package com.flink.sql;

import com.flink.bean.UserBehavior;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;

import java.time.Duration;

import static org.apache.flink.table.api.Expressions.$;

/**
 * @Author wangziyu1
 * @Date 2022/12/6 19:36
 * @Version 1.0
 * 演示数据流转换成动态表
 */
public class DynamicTableDemo {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);

        SingleOutputStreamOperator<UserBehavior> stream = env.readTextFile("/Users/wangziyu/Desktop/UserBehavior.csv")
                .flatMap(new FlatMapFunction<String, UserBehavior>() {
                    @Override
                    public void flatMap(String in, Collector<UserBehavior> out) throws Exception {
                        String[] value = in.split(",");

                        //注意时间戳要转化
                        UserBehavior userBehavior = new UserBehavior(Long.parseLong(value[0]), Long.parseLong(value[1])
                                , Integer.parseInt(value[2]), value[3], Long.parseLong(value[4]) * 1000L);


                        if (userBehavior.getBehavior().equals("pv")) {
                            out.collect(userBehavior);
                        }
                    }
                })
                .assignTimestampsAndWatermarks(WatermarkStrategy.<UserBehavior>forBoundedOutOfOrderness(Duration.ofSeconds(0)).withTimestampAssigner(new SerializableTimestampAssigner<UserBehavior>() {
                    @Override
                    public long extractTimestamp(UserBehavior userBehavior, long l) {
                        return userBehavior.getTimestamp();
                    }
                }));

        //获取表的执行环境
        StreamTableEnvironment streamTableEnvironment = StreamTableEnvironment.create(env, EnvironmentSettings.newInstance().inStreamingMode().build());

        //将数据流转换成动态表
        Table table = streamTableEnvironment.fromDataStream(stream,
                $("userId"),
                $("itemId"),
                $("categoryId"),
                $("behavior"),
                // .rowtime表示这一列是事件时间
                $("ts").rowtime());

        //将动态表转换成数据流 +I 表示insert操作
        DataStream<Row> result = streamTableEnvironment.toChangelogStream(table);
        result.print();

        env.execute();
    }
}
