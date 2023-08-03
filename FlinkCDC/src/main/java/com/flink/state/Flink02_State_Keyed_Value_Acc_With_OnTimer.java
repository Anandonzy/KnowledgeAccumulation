package com.flink.state;

import lombok.*;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.util.Collector;

import java.util.Random;

/**
 * @Author wangziyu1
 * @Date 2022/11/4 10:03
 * @Version 1.0
 */
public class Flink02_State_Keyed_Value_Acc_With_OnTimer {


    public static void main(String[] args) throws Exception {


        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.addSource(new IntSource())
                .keyBy(r -> "number")
                .process(new IntStatistic())
                .print();

        env.execute();
    }

    private static class IntSource implements SourceFunction<Integer> {

        private Boolean running = true;
        private Random random = new Random();

        @Override
        public void run(SourceContext<Integer> ctx) throws Exception {

            while (running) {
                ctx.collect(random.nextInt(100));
                Thread.sleep(1000);
            }

        }

        @Override
        public void cancel() {
            running = false;
        }
    }

    private static class IntStatistic extends KeyedProcessFunction<String, Integer, IntStatistic2> {

        private ValueState<IntStatistic2> acc;
        private ValueState<Integer> flag;

        @Override
        public void open(Configuration parameters) throws Exception {
            acc = getRuntimeContext().getState(new ValueStateDescriptor<IntStatistic2>("acc", IntStatistic2.class));
            flag = getRuntimeContext().getState(new ValueStateDescriptor<Integer>("flag", Integer.class));
        }

        @Override
        public void processElement(Integer value, Context ctx, Collector<IntStatistic2> out) throws Exception {

            //第一条数据
            if (acc.value() == null) {

                acc.update(new IntStatistic2(
                        value,
                        value,
                        value,
                        1,
                        value
                ));
            } else
            //之后过来的数据读取状态累加然后输出
            {
                IntStatistic2 oldAcc = acc.value();
                IntStatistic2 newAcc = new IntStatistic2(Math.min(value, oldAcc.min),
                        Math.max(value, oldAcc.max),
                        oldAcc.sum + value,
                        1 + oldAcc.cnt,
                        (oldAcc.sum + value) / (1 + oldAcc.cnt));
                acc.update(newAcc);
            }
            //如果不存在数据 则可以注册定时器
            if (flag.value() == null) {
                //注册一个十秒的定时器
                ctx.timerService().registerProcessingTimeTimer(ctx.timerService().currentProcessingTime() + 10 * 1000L);
                flag.update(1);
            }
        }

        @Override
        public void onTimer(long timestamp, OnTimerContext ctx, Collector<IntStatistic2> out) throws Exception {
            //定时器向下游发送结果
            out.collect(acc.value());
            //发送完数据 将定时器状态位置 置位空
            flag.clear();
        }
    }

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    private static class IntStatistic2 {
        public Integer min;
        public Integer max;
        public Integer sum;
        public Integer cnt;
        public Integer avg;
    }
}
