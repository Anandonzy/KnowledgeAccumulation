package com.flink.api.transform;

import com.flink.bean.WaterSensor;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import scala.Tuple2;

import java.util.ArrayList;

/**
 * @version 1.0
 * @Author wangziyu1
 * @Date 9/17/21 3:47 PM
 * @注释
 */
public class Flink01_TransForm_Process_Function {


    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        ArrayList<WaterSensor> waterSensors = new ArrayList<>();
        waterSensors.add(new WaterSensor("sensor_1", 1607527992000L, 20));
        waterSensors.add(new WaterSensor("sensor_1", 1607527994000L, 50));
        waterSensors.add(new WaterSensor("sensor_1", 1607527996000L, 50));
        waterSensors.add(new WaterSensor("sensor_2", 1607527993000L, 10));
        waterSensors.add(new WaterSensor("sensor_2", 1607527995000L, 30));


        /*env
                .fromCollection(waterSensors)
                .process(new ProcessFunction<WaterSensor, Tuple2<String, Integer>>() {
                    @Override
                    public void processElement(WaterSensor waterSensor, Context context, Collector<Tuple2<String, Integer>> collector) throws Exception {
                        collector.collect(new Tuple2<>(waterSensor.getId(), waterSensor.getVc()));
                    }
                }).print();*/


        env.fromCollection(waterSensors)
                .keyBy(WaterSensor::getId)
                .process(new KeyedProcessFunction<String, WaterSensor, Tuple2<String, Integer>>() {
                    @Override
                    public void processElement(WaterSensor waterSensor, Context context, Collector<Tuple2<String, Integer>> collector) throws Exception {
                        collector.collect(new Tuple2<>("key is :" + context.getCurrentKey(), waterSensor.getVc()));
                    }
                }).print();
        env.execute();


    }
}
