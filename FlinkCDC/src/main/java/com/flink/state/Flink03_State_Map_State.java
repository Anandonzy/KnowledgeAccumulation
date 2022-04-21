package com.flink.state;

import com.flink.bean.WaterSensor;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

/**
 * @version 1.0
 * @Author wangziyu1
 * @Date 9/28/21 4:13 PM
 * @注释
 */
public class Flink03_State_Map_State {


    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment
                .getExecutionEnvironment()
                .setParallelism(3);
        env
                .socketTextStream("localhost", 9999)
                .map(value -> {
                    String[] datas = value.split(",");
                    return new WaterSensor(datas[0], Long.valueOf(datas[1]), Integer.valueOf(datas[2]));

                })
                .keyBy(WaterSensor::getId)
                .process(new KeyedProcessFunction<String, WaterSensor, WaterSensor>() {

                    private MapState<Integer, String> mapState;

                    @Override
                    public void open(Configuration parameters) throws Exception {
                        getRuntimeContext().getMapState(new MapStateDescriptor<Integer, String>("mapState", Integer.class, String.class));
                    }

                    @Override
                    public void processElement(WaterSensor value, Context context, Collector<WaterSensor> out) throws Exception {

                        if (!mapState.contains(value.getVc())) {
                            out.collect(value);
                            mapState.put(value.getVc(), "随意");
                        }

                    }
                }).print();

        env.execute();

    }
}
