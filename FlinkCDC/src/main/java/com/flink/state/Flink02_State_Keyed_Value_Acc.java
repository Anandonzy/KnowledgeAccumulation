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
 * @Date 2022/11/4 00:44
 * @Version 1.0
 * 使用ValueState 实现求最小值 最大值 求和 条数 平均值
 */
public class Flink02_State_Keyed_Value_Acc {
    public static void main(String[] args) throws Exception {


        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.addSource(new IntSource())
                .keyBy(r -> "number")
                .process(new Statistic())
                .print();

        env.execute();
    }


    private static class IntSource implements SourceFunction<Integer> {

        private Boolean running = true;
        private final Random random = new Random();

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

    private static class Statistic extends KeyedProcessFunction<String, Integer, IntStatistic> {

        private ValueState<IntStatistic> acc;

        @Override
        public void open(Configuration parameters) throws Exception {
            acc = getRuntimeContext().getState(new ValueStateDescriptor<IntStatistic>("acc", IntStatistic.class));
        }

        @Override
        public void processElement(Integer value, KeyedProcessFunction<String, Integer, IntStatistic>.Context ctx, Collector<IntStatistic> out) throws Exception {

            //第一条数据来的时候
            //取出来当前key 所对应的状态变量
            if (acc.value() == null) {
                acc.update(new IntStatistic(value, value, value, 1, value));
            } else {
                IntStatistic oldAcc = acc.value();

                IntStatistic newAcc = new IntStatistic(
                        Math.min(oldAcc.min, value),
                        Math.max(oldAcc.max, value),
                        oldAcc.sum + value,
                        oldAcc.cnt + 1,
                        (oldAcc.sum + value) / (oldAcc.cnt + 1));
                acc.update(newAcc);
            }

            out.collect(acc.value());
        }
    }

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    private static class IntStatistic {
        public Integer min;
        public Integer max;
        public Integer sum;
        public Integer cnt;
        public Integer avg;
    }
}
