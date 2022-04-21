package com.flink.watermark;

import com.flink.bean.WaterSensor;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.time.Duration;

/**
 * @version 1.0
 * @Author wangziyu1
 * @Date 9/24/21 4:14 PM
 * @注释
 */
public class Flink10_Chapter07_OrderedWaterMark {


    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment().setParallelism(1);

        SingleOutputStreamOperator<WaterSensor> stream = env.socketTextStream("localhost", 9999)
                .map(new MapFunction<String, WaterSensor>() {
                    @Override
                    public WaterSensor map(String value) throws Exception {

                        String[] datas = value.split(",");
                        return new WaterSensor(datas[0], Long.valueOf(datas[1]), Integer.valueOf(datas[2]));
                    }
                });

        //创建水印生产策略
        WatermarkStrategy<WaterSensor> wms = WatermarkStrategy
//                最大容忍的延迟的时间
                .<WaterSensor>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                .withTimestampAssigner(new SerializableTimestampAssigner<WaterSensor>() { //指定时间戳
                    @Override
                    public long extractTimestamp(WaterSensor element, long l) {
                        return element.getTs() * 1000;
                    }
                });

        stream.assignTimestampsAndWatermarks(wms)
                .keyBy(WaterSensor::getId)
                .window(TumblingEventTimeWindows.of(Time.seconds(5)))
                .process(new ProcessWindowFunction<WaterSensor, String, String, TimeWindow>() {
                    @Override
                    public void process(String key, Context context, Iterable<WaterSensor> element, Collector<String> collector) throws Exception {
                        String msg = "当前key:" + key + "窗口:[" + context.window().getStart() / 1000 + "," + context.window().getEnd() / 1000 + "] 一共有:" + element.spliterator().estimateSize() + "条数据";
                        collector.collect(msg);
                    }
                }).print();

        env.execute();
    }
}
