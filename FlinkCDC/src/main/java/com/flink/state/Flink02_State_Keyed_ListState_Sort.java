package com.flink.state;

import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

/**
 * @Author wangziyu1
 * @Date 2022/11/7 14:11
 * @Version 1.0
 * 使用ListState 对历史数据进行排序
 */
public class Flink02_State_Keyed_ListState_Sort {


    public static void main(String[] args) throws Exception {


        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.addSource(new IntStatistic())
                .keyBy(i -> i % 2)
                .process(new HistorySort())
                .print();
        env.execute();
    }


    private static class IntStatistic implements SourceFunction<Integer> {

        private Boolean isRunning = true;
        private final Random random = new Random();

        @Override
        public void run(SourceContext sourceContext) throws Exception {

            while (isRunning) {
                sourceContext.collect(random.nextInt(100));
                Thread.sleep(1000L);
            }
        }

        @Override
        public void cancel() {
            isRunning = false;
        }
    }

    private static class HistorySort extends KeyedProcessFunction<Integer, Integer, String> {

        private ListState<Integer> history;

        @Override
        public void onTimer(long timestamp, OnTimerContext ctx, Collector<String> out) throws Exception {
            //10s 到达之后清空状态继续排序
            history.clear();
        }

        @Override
        public void open(Configuration parameters) throws Exception {
            history = getRuntimeContext().getListState(
                    new ListStateDescriptor<Integer>("history", Integer.class));
        }


        @Override
        public void processElement(Integer in, Context ctx, Collector<String> out) throws Exception {

            ArrayList<Integer> list = new ArrayList<>();
            //将数据in 添加到key的状态里面
            history.add(in);

            //将数据从history 里面取出然后放到ArrayList 里面进行排序
            //返回的是所有元素
            for (Integer v : history.get()) {
                list.add(v);
            }

            list.sort(new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return o1 - o2;
                }
            });
            //当长度达到10之后 然后注册定时器10s 清空缓存
            if (list.size() == 10) {
                ctx.timerService().registerProcessingTimeTimer(ctx.timerService().currentProcessingTime() + 10 * 1000L);
            }

            //格式化输出 根据不同的key进行相应的格式化输出
            StringBuilder sb = new StringBuilder();
            if (ctx.getCurrentKey() == 0) {
                sb.append("偶数的历史数据 ");
            } else {
                sb.append("奇数的历史数据 ");
            }

            for (Integer v : list) {
                sb.append(v).append("->");
            }
            out.collect(sb.toString());
        }
    }
}
