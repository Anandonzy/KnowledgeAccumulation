package com.flink.ads;


import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.TreeSet;

/**
 * @version 1.0
 * @Author wangziyu1
 * @Date 10/15/21 2:52 PM
 * @注释
 */
public class Flink04_Project_Page_TopN {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        // 创建WatermarkStrategy
//        WatermarkStrategy<ApacheLog> wms = WatermarkStrategy
//                .<ApacheLog>forBoundedOutOfOrderness(Duration.ofSeconds(60))
//                .withTimestampAssigner(new SerializableTimestampAssigner<ApacheLog>() {
//                    @Override
//                    public long extractTimestamp(ApacheLog element, long recordTimestamp) {
//                        return element.getEventTime();
//                    }
//                });


    }
}
