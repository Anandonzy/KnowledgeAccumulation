package com.flink.cep;


import akka.actor.SelectParent;
import com.flink.bean.LoginEvent;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * @version 1.0
 * @Author wangziyu1
 * @Date 10/20/21 11:42 AM
 * @注释
 */
public class Flink01_CEP_Project_Login {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);
        // 创建WatermarkStrategy
        WatermarkStrategy<LoginEvent> mws = WatermarkStrategy.<LoginEvent>forBoundedOutOfOrderness(Duration.ofSeconds(20))
                .withTimestampAssigner(new SerializableTimestampAssigner<LoginEvent>() {
                    @Override
                    public long extractTimestamp(LoginEvent element, long recordTimestamp) {
                        return element.getEventTime();
                    }
                });


        KeyedStream<LoginEvent, Long> loginKS = env.readTextFile("/Users/wangziyu/Desktop/data/LoginLog.csv")
                .map(line -> {
                    String[] data = line.split(",");
                    return new LoginEvent(Long.valueOf(data[0]),
                            data[1],
                            data[2],
                            Long.parseLong(data[3]) * 1000L);

                }).assignTimestampsAndWatermarks(mws)
                .keyBy(LoginEvent::getUserId);

        // 1. 定义模式
        Pattern<LoginEvent, LoginEvent> failPattern = Pattern.<LoginEvent>begin("fail")
                .where(new SimpleCondition<LoginEvent>() {
                    @Override
                    public boolean filter(LoginEvent value) throws Exception {
                        return "fail".equals(value.getEventType());
                    }
                })
                //两秒内 有多于两次的登录就是恶意登录
                .timesOrMore(2).consecutive().within(Time.seconds(2));


        // 2. 检测模式
        PatternStream<LoginEvent> failedStream = CEP.pattern(loginKS, failPattern);

        failedStream.select(new PatternSelectFunction<LoginEvent, String>() {

            @Override
            public String select(Map<String, List<LoginEvent>> result) throws Exception {
                return result.get("fail").toString();
            }
        }).print();

        env.execute();
    }

}
