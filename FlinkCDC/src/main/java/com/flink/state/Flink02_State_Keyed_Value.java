package com.flink.state;

import com.flink.bean.WaterSensor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;


/**
 * @version 1.0
 * @Author wangziyu1
 * @Date 9/27/21 4:46 PM
 * @注释 检测传感器的温度值，如果连续的两个温度差值超过10度，就输出报警。
 */
public class Flink02_State_Keyed_Value {

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
                .process(new KeyedProcessFunction<String, WaterSensor, String>() {

                    //使用那个状态算子 就用哪个
                    private ValueState<Integer> state;

                    //java编写的话需要重写open方法,scala编写话可以使用lazy
                    @Override
                    public void open(Configuration parameters) throws Exception {
                        state = getRuntimeContext().getState(new ValueStateDescriptor<Integer>("state", Integer.class));
                    }

                    @Override
                    public void processElement(WaterSensor value, Context ctx, Collector<String> collector) throws Exception {

                        Integer lastVc = state.value() == null ? 0 : state.value();

                        //检测传感器的温度值，如果连续的两个温度差值超过10度，就输出报警。
                        if (Math.abs(value.getVc() - lastVc) >= 10) {
                            collector.collect(value.getId() + " 红色警报!!!");
                        }
                        state.update(value.getVc());
                    }
                })
                .print();

        env.execute();
    }
}
