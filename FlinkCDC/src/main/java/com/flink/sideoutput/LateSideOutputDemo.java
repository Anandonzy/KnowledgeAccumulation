package com.flink.sideoutput;

import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

/**
 * @Author wangziyu1
 * @Date 2022/11/14 14:47
 * @Version 1.0
 * 演示迟到数据如何处理
 * 使用测输出流
 */
public class LateSideOutputDemo {


    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);

        SingleOutputStreamOperator<String> result = env.addSource(new SourceFunction<String>() {
                    @Override
                    public void run(SourceContext<String> ctx) throws Exception {

                        ctx.collectWithTimestamp("a", 1000L);
                        ctx.emitWatermark(new Watermark(999L));

                        ctx.collectWithTimestamp("a", 2000);
                        ctx.emitWatermark(new Watermark(1999L));

                        //这一条就是迟到数据
                        ctx.collectWithTimestamp("a", 1500L);
                        ctx.emitWatermark(new Watermark(999L));
                    }

                    @Override
                    public void cancel() {

                    }
                })
                .process(new ProcessFunction<String, String>() {
                    @Override
                    public void processElement(String in, Context ctx, Collector<String> out) throws Exception {

                        if (ctx.timestamp() > ctx.timerService().currentWatermark()) { //没有迟到的数据
                            out.collect("数据" + in + "的当前时间是:" + ctx.timestamp()
                                    + ",当前的水位线是:" + ctx.timerService().currentWatermark());
                        } else { //迟到的数据

                            ctx.output(new OutputTag<String>("lateOut") {
                            }, "数据:" + in + "的事件时间是:"
                                    + ctx.timestamp() + ",当前水位线是" + ctx.timerService().currentWatermark());
                        }

                    }
                });

        result.print("主流:");

        result.getSideOutput(new OutputTag<String>("lateOut"){}).print("测输出流:");


        env.execute();


    }
}
