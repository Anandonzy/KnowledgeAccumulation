package com.flink.state;

import com.flink.api.soruce.Sensor;
import com.flink.api.soruce.SensorSource;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

/**
 * @Author wangziyu1
 * @Date 2022/11/8 09:39
 * @Version 1.0
 * 传感器连续1s上升的程序
 */
public class State_Value_State_Continuous_1S_High {


    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);

        env.addSource(new SensorSource())
                .keyBy(Sensor::getSensorName)
                .process(new SensorProcessor())
                .print();


        env.execute();


    }

    /**
     * 处理逻辑
     */
    private static class SensorProcessor extends KeyedProcessFunction<String, Sensor, String> {

        private ValueState<Double> lastTemperatureState;

        private ValueState<Long> lastTsState;

        @Override
        public void open(Configuration parameters) throws Exception {
            lastTemperatureState = getRuntimeContext().getState(
                    new ValueStateDescriptor<Double>("last_temperature", Types.DOUBLE));

            lastTsState = getRuntimeContext().getState(
                    new ValueStateDescriptor<Long>("last_ts", Long.class));
        }

        @Override
        public void processElement(Sensor sensor, Context ctx, Collector<String> out) throws Exception {
            //第一条数据状态为null ,之后取出的是上一条的温度
            Double lastTemp = lastTemperatureState.value();
            Long ts = lastTsState.value();

            //更新当前的温度到状态里面
            lastTemperatureState.update(sensor.getSensorValue());

            //当存在状态的时候
            if (lastTemp != null) {

                //温度上升 且没有当前温度的定时器需要注册一个1s之后报警的定时器
                if (sensor.getSensorValue() > lastTemp && ts == null) {
                    long nextOneSecond = ctx.timerService().currentProcessingTime() + 1000L;
                    //注册一个定时器
                    ctx.timerService().registerProcessingTimeTimer(nextOneSecond);
                    //将定时器的时间戳保存下来
                    lastTsState.update(nextOneSecond);
                }
                //温度下降且存在定时器 需要移除当前key的定时器
                else if (sensor.getSensorValue() < lastTemp && ts != null) {
                    ctx.timerService().deleteProcessingTimeTimer(ts);
                    //将保存定时器的时间戳也要删除
                    lastTsState.clear();
                }
            }
        }

        /**
         * 报警定时器 输出报警信息
         *
         * @param timestamp
         * @param ctx
         * @param out
         * @throws Exception
         */
        @Override
        public void onTimer(long timestamp, OnTimerContext ctx, Collector<String> out) throws Exception {
            out.collect(ctx.getCurrentKey() + " 1s内温度升高了~");
            lastTsState.clear();
        }
    }
}
