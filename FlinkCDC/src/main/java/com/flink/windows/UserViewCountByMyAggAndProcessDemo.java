package com.flink.windows;

import com.flink.api.soruce.ClickSource;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

/**
 * @Author wangziyu1
 * @Date 2022/11/9 01:10
 * @Version 1.0
 * 自己实现AggregateFunction 和 ProcessWindowFunction 的逻辑
 * 帮助理解底层实现原理
 * 自己实现AggregateFunction 的累加器的原理 然后 结果输出
 * mapState + OnTimer 实现
 */
public class UserViewCountByMyAggAndProcessDemo {


    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);


        env.addSource(new ClickSource())
                .keyBy(ClickSource.Click::getName)
                .process(new MyAggAndProcessResult(10 * 1000L))
                .print();

        env.execute();
    }

    private static class MyAggAndProcessResult extends KeyedProcessFunction<String, ClickSource.Click, UserViewCountPreWindow> {
        private final long windwoSize;
        private MapState<Tuple2<Long, Long>, Long> mapState;

        public MyAggAndProcessResult(long windwoSize) {
            this.windwoSize = windwoSize;
        }

        @Override
        public void open(Configuration parameters) throws Exception {
            mapState = getRuntimeContext().getMapState(new MapStateDescriptor<Tuple2<Long, Long>, Long>(
                    "acc-state",
                    Types.TUPLE(Types.LONG, Types.LONG),
                    Types.LONG
            ));
        }

        @Override
        public void processElement(ClickSource.Click in, Context ctx, Collector<UserViewCountPreWindow> out) throws Exception {

            //根据传来的数据获取窗口的信息
            //拼接窗口的信息 Tuple2<窗口的开始时间,窗口的结束时间>
            long currTs = ctx.timerService().currentProcessingTime();
            long windowStart = currTs - currTs % windwoSize;
            long windowEnd = windowStart + windwoSize;
            Tuple2<Long, Long> windowInfo = Tuple2.of(windowStart, windowEnd);

            //判断当前键控流里面的状态 是否含有当前的窗口信息windowInfo
            if (!mapState.contains(windowInfo)) { //不包含当前窗口的信息
                mapState.put(windowInfo, 1L);
            } else {
                Long oldAcc = mapState.get(windowInfo);
                Long newAcc = oldAcc + 1L; //取出来当前窗口的值 然后累加器加1 这个地方就是AggregateFunction 里面的累加器加1的逻辑
                mapState.put(windowInfo, newAcc); //AggregateFunction getResult 方法 定时器触发获取结果即可
            }

            //注册定时器 (windowEnd -1L)
            ctx.timerService().registerProcessingTimeTimer(windowEnd - 1L);
        }

        @Override
        public void onTimer(long timerTs, OnTimerContext ctx, Collector<UserViewCountPreWindow> out) throws Exception {

            //恢复窗口的信息
            long windowEnd = timerTs + 1L;
            long windowStart = windowEnd - windwoSize;
            Tuple2<Long, Long> windowInfo = Tuple2.of(windowStart, windowEnd);
            String userName = ctx.getCurrentKey();

            //输出信息
            out.collect(new UserViewCountPreWindow(userName,
                    mapState.get(windowInfo), windowStart, windowEnd));
            //定时器触发移除当前窗口
            mapState.remove(windowInfo);
        }
    }
}
