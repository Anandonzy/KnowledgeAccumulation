package com.flink.api.transform;

import com.flink.bean.WaterSensor;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.ArrayList;

/**
 * @version 1.0
 * @Author wangziyu1
 * @Date 9/17/21 3:14 PM
 * @注释 **常见的滚动聚合算子:**
 * <p>
 * > sum, min,max
 * minBy,maxBy
 */
public class Flink01_TransForm_Aggregation_Function {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

     /*   DataStreamSource<Integer> stream = env.fromElements(1, 2, 3, 4, 5);
        KeyedStream<Integer, String> keyedStream = stream.keyBy(ele -> ele % 2 == 0 ? "奇数" : "偶数");
        keyedStream.max(0).print("max");
        keyedStream.min(0).print("min");
        keyedStream.sum(0).print("sum");
*/

     /*   ArrayList<WaterSensor> waterSensors = new ArrayList<>();
        waterSensors.add(new WaterSensor("sensor_1", 1607527992000L, 20));
        waterSensors.add(new WaterSensor("sensor_1", 1607527994000L, 50));
        waterSensors.add(new WaterSensor("sensor_1", 1607527996000L, 30));
        waterSensors.add(new WaterSensor("sensor_2", 1607527993000L, 10));
        waterSensors.add(new WaterSensor("sensor_2", 1607527995000L, 30));

        KeyedStream<WaterSensor, String> kbStream = env
                .fromCollection(waterSensors)
                .keyBy(WaterSensor::getId);

        kbStream
                .sum("vc")
                .print("maxBy...");


*/

        ArrayList<WaterSensor> waterSensors = new ArrayList<>();
        waterSensors.add(new WaterSensor("sensor_1", 1607527992000L, 20));
        waterSensors.add(new WaterSensor("sensor_1", 1607527994000L, 50));
        waterSensors.add(new WaterSensor("sensor_1", 1607527996000L, 50));
        waterSensors.add(new WaterSensor("sensor_2", 1607527993000L, 10));
        waterSensors.add(new WaterSensor("sensor_2", 1607527995000L, 30));

        KeyedStream<WaterSensor, String> kbStream = env
                .fromCollection(waterSensors)
                .keyBy(WaterSensor::getId);

        kbStream
                .maxBy("vc", false)
                .print("max...");

        env.execute();


    }
}
