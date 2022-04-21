package com.flink.api.pv;

import com.flink.bean.UserBehavior;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.time.Duration;

/**
 * @version 1.0
 * @Author wangziyu1
 * @Date 10/14/21 11:40 AM
 * @注释 带时间窗口的pv计算方式
 */
public class Flink01_Project_Product_PV {


    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 创建WatermarkStrategy
        WatermarkStrategy<UserBehavior> wms = WatermarkStrategy
                .<UserBehavior>forBoundedOutOfOrderness(Duration.ofSeconds(5))
                .withTimestampAssigner(new SerializableTimestampAssigner<UserBehavior>() {
                    @Override
                    public long extractTimestamp(UserBehavior element, long recordTimestamp) {
                        return element.getTimestamp() * 1000L;
                    }
                });

        env
                .readTextFile("/Users/wangziyu/Desktop/UserBehavior.csv")
                .map(line -> { // 对数据切割, 然后封装到POJO中
                    String[] split = line.split(",");
                    return new UserBehavior(Long.valueOf(split[0]), Long.valueOf(split[1]), Integer.valueOf(split[2]), split[3], Long.valueOf(split[4]));
                })
                .filter(behavior -> "pv".equals(behavior.getBehavior())) //过滤出pv行为
                .assignTimestampsAndWatermarks(wms)
                .map(behavior -> Tuple2.of("pv", 1L))
                .returns(Types.TUPLE(Types.STRING, Types.LONG)) //使用Tupl的方式 方便后续求和使用
                .keyBy(value -> value.f0) //根据key分组
                .window(TumblingEventTimeWindows.of(Time.minutes(60))) //分配时间窗口
                .sum(1)//求和
                .print();

        env.execute();
    }
}
