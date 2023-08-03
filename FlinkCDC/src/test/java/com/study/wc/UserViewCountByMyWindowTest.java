package com.study.wc;

import com.flink.api.soruce.ClickSource;
import com.flink.windows.UserViewCountPreWindow;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author wangziyu1
 * @Date 2022/11/8 16:14
 * @Version 1.0
 * 使用keyProcessFunction代替ProcessWindowFunction 实现窗口计算的功能
 */
public class UserViewCountByMyWindowTest {


    public static void main(String[] args) throws Exception {


        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);

        env.addSource(new ClickSource())
                .keyBy(ClickSource.Click::getName)
                .process(new ClickResult(10 * 1000L))
                .print();


        env.execute();


    }

    private static class ClickResult extends KeyedProcessFunction<String, ClickSource.Click, UserViewCountPreWindow> {

        private long windowSize;

        public ClickResult(long windowSize) {
            this.windowSize = windowSize;
        }

        private MapState<Tuple2<Long, Long>, List<ClickSource.Click>> mapState;


        @Override
        public void open(Configuration parameters) throws Exception {

            mapState = getRuntimeContext().getMapState(new MapStateDescriptor<Tuple2<Long, Long>, List<ClickSource.Click>>(
                    "map-state",
                    Types.TUPLE(Types.LONG, Types.LONG),
                    Types.LIST(Types.POJO(ClickSource.Click.class))
            ));
        }

        @Override
        public void processElement(ClickSource.Click in, Context ctx, Collector<UserViewCountPreWindow> out) throws Exception {
            //根据数据获取当前窗口的时间
            long currTs = ctx.timerService().currentProcessingTime();
            long windowStart = currTs - currTs % windowSize;
            long windowEnd = windowStart + windowSize;
            Tuple2<Long, Long> windowInfo = Tuple2.of(windowStart, windowEnd);


            //判断当前的数据是否在状态里面
            //当前状态没有这个窗口需要创建一个
            if (!mapState.contains(windowInfo)) {
                //新建一个list
                ArrayList<ClickSource.Click> list = new ArrayList<>();
                list.add(in);
                mapState.put(windowInfo, list);
            } else {
                mapState.get(windowInfo).add(in);
            }

            //注册一个定时器 (windowEnd-1s)
            ctx.timerService().registerProcessingTimeTimer(windowEnd - 1L);
        }

        /**
         * 定时器 移除窗口状态 输出信息
         * @param ts
         * @param ctx
         * @param out
         * @throws Exception
         */
        @Override
        public void onTimer(long ts, OnTimerContext ctx, Collector<UserViewCountPreWindow> out) throws Exception {
            //还原窗口信息
            long windowEnd = ts + 1L;
            long windowStart = windowEnd - windowSize;
            //窗口信息
            Tuple2<Long, Long> windowInfo = Tuple2.of(windowStart, windowEnd);


            //输出窗口结果
            String currentKey = ctx.getCurrentKey();
            out.collect(new UserViewCountPreWindow(currentKey,
                    mapState.get(windowInfo).size(),
                    windowStart,
                    windowEnd));


            //销毁窗口
            mapState.remove(windowInfo);
        }
    }
}
