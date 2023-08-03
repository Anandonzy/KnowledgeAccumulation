package com.flink.windows;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.util.Collector;

/**
 * @Author wangziyu1
 * @Date 2022/11/10 11:34
 * @Version 1.0
 * 水位线连续升高三次就实现报警
 */
public class SenSorHightDemo {


    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);

        env.addSource(new SourceFunction<Integer>() {
                    @Override
                    public void run(SourceContext<Integer> ctx) throws Exception {
                        ctx.collect(1);
                        Thread.sleep(300L);
                        ctx.collect(2);
                        Thread.sleep(300L);

                        ctx.collect(3);
                        Thread.sleep(300L);
                        ctx.collect(4);
                        Thread.sleep(300L);
                    }

                    @Override
                    public void cancel() {

                    }
                })
                .keyBy(r -> "number")
                .process(new KeyedProcessFunction<String, Integer, String>() {

                    private ValueState<Integer> count;

                    private ValueState<Integer> lastTemp;

                    @Override
                    public void open(Configuration parameters) throws Exception {
                        count = getRuntimeContext().getState(new ValueStateDescriptor<Integer>("count", Types.INT));
                        lastTemp = getRuntimeContext().getState(new ValueStateDescriptor<Integer>("last-temp", Types.INT));
                    }

                    @Override
                    public void processElement(Integer in, Context ctx, Collector<String> out) throws Exception {
                        // 当地一条数据过来的时候
                        if (count.value() == null) {
                            count.update(0);
                        }
                        //不是第一条了 取出来上一条的温度值
                        Integer lastTempValue = this.lastTemp.value();
                        lastTemp.update(in);


                        //lastTempValue 不为空的时候 为空不处理即可
                        if (lastTempValue != null) {
                            if (in > lastTempValue) {
                                count.update(count.value() + 1);
                            } else {
                                //没有连续升高的时候 直接处理为0
                                count.update(0);
                            }
                        }

                        if (count.value() >= 3) {
                            out.collect("温度连续上升3次了,需要处理~");
                            count.update(0);
                        }
                    }
                })
                .print();

        env.execute();


    }
}
