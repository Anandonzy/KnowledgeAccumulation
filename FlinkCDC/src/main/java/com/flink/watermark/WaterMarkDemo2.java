package com.flink.watermark;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.util.Collector;

/**
 * @Author wangziyu1
 * @Date 2022/11/10 15:39
 * @Version 1.0
 * 证明watermark的特性
 * 1. Flink会在流的最开始插入一个时间戳为负无穷大的水位线
 * 2. Flink会在流的最末尾插入一个时间戳为正无穷大的水位线
 */
public class WaterMarkDemo2 {

    public static void main(String[] args) throws Exception {


        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        env.addSource(new SourceFunction<Integer>() {
                    @Override
                    public void run(SourceContext<Integer> ctx) throws Exception {
                        //1.要发送的事件
                        //2.事件时间
                        ctx.collectWithTimestamp(1, 1000L);
                    }

                    @Override
                    public void cancel() {

                    }
                }).keyBy(r -> "number")
                .process(new KeyedProcessFunction<String, Integer, String>() {
                    @Override
                    public void processElement(Integer in, Context ctx, Collector<String> out) throws Exception {
                        //ctx.timeStamp() 获取输入数据的事件时间. 事件时间+5s
                        ctx.timerService().registerEventTimeTimer(ctx.timestamp() + 5000L);
                        out.collect("数据:" + in +
                                "到达,当前KeyProcessFunction 的并行子任务的水位线是:" +
                                ctx.timerService().currentWatermark());
                    }

                    @Override
                    public void onTimer(long timerTs, OnTimerContext ctx, Collector<String> out) throws Exception {
                        out.collect("时间是:" + timerTs + " 的定时器触发"
                                + "当前KeyedProcessFunction 的并行子任务的水位线是" + ctx.timerService().currentWatermark());


                    }
                }).print();


        env.execute();
    }


}
