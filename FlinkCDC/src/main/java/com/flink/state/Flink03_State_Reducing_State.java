package com.flink.state;

import akka.stream.impl.ReducerState;
import com.flink.bean.WaterSensor;
import org.apache.flink.api.common.state.ReducingState;
import org.apache.flink.api.common.state.ReducingStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.util.List;

/**
 * @version 1.0
 * @Author wangziyu1
 * @Date 9/28/21 3:56 PM
 * @注释
 */
public class Flink03_State_Reducing_State {


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
                .process(new KeyedProcessFunction<String, WaterSensor, Integer>() {

                    private ReducingState<Integer> sumVcState = null;

                    @Override
                    public void processElement(WaterSensor value, Context ctx, Collector<Integer> out) throws Exception {
                        sumVcState.add(value.getVc());
                        out.collect(sumVcState.get());
                    }



                    @Override
                    public void open(Configuration parameters) throws Exception {
                        sumVcState = getRuntimeContext().getReducingState(new ReducingStateDescriptor<Integer>("reduceer-statue", Integer::sum, Integer.class));
                    }
                })
                .print();


        env.execute();
    }
}
