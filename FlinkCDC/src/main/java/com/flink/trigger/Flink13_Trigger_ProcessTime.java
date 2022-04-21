package com.flink.trigger;

import com.flink.bean.WaterSensor;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

/**
 * @version 1.0
 * @Author wangziyu1
 * @Date 9/26/21 3:41 PM
 * @注释
 */
public class Flink13_Trigger_ProcessTime {


    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment().setParallelism(1);


        SingleOutputStreamOperator<WaterSensor> stream = env
                .socketTextStream("localhost", 9999)  // 在socket终端只输入毫秒级别的时间戳
                .map(value -> {
                    String[] datas = value.split(",");
                    return new WaterSensor(datas[0], Long.valueOf(datas[1]), Integer.valueOf(datas[2]));
                });


        stream
                .keyBy(WaterSensor::getId)

                .process(new KeyedProcessFunction<String, WaterSensor, String>() {
                    @Override
                    public void processElement(WaterSensor waterSensor, Context context, Collector<String> collector) throws Exception {
                        //处理时间过后5s出发定时器
                        context.timerService().registerProcessingTimeTimer(context.timerService().currentProcessingTime() + 5000);
                        collector.collect(waterSensor.toString());
                    }

                    // 定时器被触发之后, 回调这个方法
                    // 参数1: 触发器被触发的时间
                    @Override
                    public void onTimer(long timestamp, OnTimerContext ctx, Collector<String> out) throws Exception {
                        System.out.println(timestamp);
                        out.collect("我被触发了....");
                    }
                }).print();

        env.execute();
    }
}
