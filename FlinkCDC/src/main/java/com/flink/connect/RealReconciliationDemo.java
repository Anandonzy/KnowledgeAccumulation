package com.flink.connect;

import lombok.*;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.util.Collector;

/**
 * @Author wangziyu1
 * @Date 2022/11/15 09:46
 * @Version 1.0
 * 借助状态 实现实时对账
 * 如果left事件先到达 则等待5s right事件,若果没等到则输出对账失败
 * 如果right事件先到达 则等待5s left事件,若果没等到则输出对账失败
 */
public class RealReconciliationDemo {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        DataStreamSource<Event> stream1 = env.addSource(new SourceFunction<Event>() {
            @Override
            public void run(SourceContext<Event> ctx) throws Exception {

                //发送带事件时间的数据
                ctx.collectWithTimestamp(new Event("key-1", "left", 1000L), 1000L);
                ctx.collectWithTimestamp(new Event("key-2", "left", 2000L), 2000L);
            }

            @Override
            public void cancel() {

            }
        });

        DataStreamSource<Event> stream2 = env.addSource(new SourceFunction<Event>() {
            @Override
            public void run(SourceContext<Event> ctx) throws Exception {

                //发送带事件时间的数据 这个地方key-1 和第一个流里面的key-1 能对账成功就是因为结束的时候会插入一个正无穷的水位线 触发定时器执行
                ctx.collectWithTimestamp(new Event("key-1", "right", 18 * 1000L), 18 * 1000L);
                ctx.collectWithTimestamp(new Event("key-3", "right", 8000L), 8000L);


            }

            @Override
            public void cancel() {

            }
        });

        stream1.keyBy(r -> r.key)
                .connect(stream2.keyBy(r -> r.key))
                .process(new Match())
                .print();


        env.execute();

    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class Event {

        public String key;
        public String value;
        public Long ts;
    }

    private static class Match extends CoProcessFunction<Event, Event, String> {

        private ValueState<Event> leftState;
        private ValueState<Event> rightState;

        @Override
        public void open(Configuration parameters) throws Exception {
            leftState = getRuntimeContext().getState(new ValueStateDescriptor<Event>("leftState", Event.class));
            rightState = getRuntimeContext().getState(new ValueStateDescriptor<Event>("rightState", Event.class));
        }

        @Override
        public void processElement1(Event in1, Context ctx, Collector<String> out) throws Exception {

            //如果left事件来了 等待5s 如果right没来就输出对账失败
            // rightState =null 则left先来
            if (rightState.value() == null) {
                //保存left的值
                leftState.update(in1);
                ctx.timerService().registerEventTimeTimer(in1.getTs() + 5000L);
            } else {
                out.collect("数据:" + in1 + "对账成功,right事件先到达.");
                rightState.clear();
            }
        }

        @Override
        public void processElement2(Event in2, Context ctx, Collector<String> out) throws Exception {

            //和 processElement1完全对称
            if (leftState.value() == null) {
                rightState.update(in2);
                ctx.timerService().registerEventTimeTimer(in2.getTs() + 5000L);
            } else {
                out.collect("数据:" + in2 + "对账成功,left 事件先到达.");
                leftState.clear();
            }
        }

        @Override
        public void onTimer(long timerTs, OnTimerContext ctx, Collector<String> out) throws Exception {

            if (leftState.value() != null) {
                out.collect(leftState.value().key + "对账失败,right 事件没来");
                leftState.clear();
            }

            if (rightState.value() != null) {
                out.collect(rightState.value().key + "对账失败,left 事件没来");
                rightState.clear();
            }
        }
    }
}
