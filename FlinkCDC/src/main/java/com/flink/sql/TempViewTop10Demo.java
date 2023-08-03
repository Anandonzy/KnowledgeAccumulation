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
 * @Date 2022/12/6 19:46
 * @Version 1.0
 * 演示临时视图 将动态表转换成临时视图
 * 每个窗口每个商品的浏览次数
 */
public class TempViewTop10Demo {

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

        //将动态表转换成临时视图
        streamTableEnvironment.createTemporaryView("userbehavior", table);

        //查询
        //count 操作试讲窗口中的元素全都收集起来然后数一遍
        //相当于只使用ProcessFunction的情况
        Table result = streamTableEnvironment.sqlQuery(
                "SELECT itemId, COUNT(itemId) as cnt," +
                " HOP_START(ts, INTERVAL '5' MINUTES, INTERVAL '1' HOURS) as windowStartTime," +
                " HOP_END(ts, INTERVAL '5' MINUTES, INTERVAL '1' HOURS) as windowEndTime" +
                " FROM userbehavior GROUP BY" +
                " itemId, " +
                " HOP(ts, INTERVAL '5' MINUTES, INTERVAL '1' HOURS)");//使用的第三方组件的原因叫HOP_START 滑动窗口

        streamTableEnvironment.toChangelogStream(result).print();


        env.execute();
    }
}
