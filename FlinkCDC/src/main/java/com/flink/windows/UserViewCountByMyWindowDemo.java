package com.flink.windows;

import com.flink.api.soruce.ClickSource;
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
 * @Date 2022/11/8 13:59
 * @Version 1.0
 * 使用keyProcessFunction实现 processWindowFunction
 * 自己维护一个MapState<Tuple2<windowStart,windowEnd>,List<UserViewCountPreWindow>>
 */
public class UserViewCountByMyWindowDemo {


    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        env.addSource(new ClickSource())
                .keyBy(ClickSource.Click::getName)
                .process(new MyTumblingWindow(10 * 1000L))
                .print();


        env.execute("UserViewCountByMyWindowDemo");


    }

    private static class MyTumblingWindow extends KeyedProcessFunction<String, ClickSource.Click, UserViewCountPreWindow> {

        private long windowSize; //接收窗口大小

        //key: Tuple2<窗口开始时间, 窗口结束时间>
        //value 窗口内的所有元素
        private MapState<Tuple2<Long, Long>, List<ClickSource.Click>> mapState;

        public MyTumblingWindow(long windowSize) {
            this.windowSize = windowSize;
        }

        @Override
        public void open(Configuration parameters) throws Exception {
            mapState = getRuntimeContext().getMapState(new MapStateDescriptor<Tuple2<Long, Long>, List<ClickSource.Click>>("windwos-elements",
                    Types.TUPLE(Types.LONG, Types.LONG),
                    Types.LIST(Types.POJO(ClickSource.Click.class))));
        }

        @Override
        public void processElement(ClickSource.Click in, Context ctx, Collector<UserViewCountPreWindow> out) throws Exception {

            //根据时间戳计算数据所属的窗口的开始时间
            //key: Tuple2<窗口开始时间, 窗口结束时间>
            long currTs = ctx.timerService().currentProcessingTime();

            long windowStart = currTs - currTs % windowSize;
            long windowEnd = windowStart + windowSize;

            //窗口信息拼接完成了
            Tuple2<Long, Long> windowInfo = Tuple2.of(windowStart, windowEnd);

            //判断mapState 是否有windowInfo这个key
            //也就是判断是否包含这个窗口
            // 如果不存在这个窗口 那么输入数据in就是第一个元素
            if (!mapState.contains(windowInfo)) {
                //新建列表
                ArrayList<ClickSource.Click> list = new ArrayList<>();

                //将输入数据加入到列表当中
                list.add(in);

                //创建一个新的窗口 当前的元素也是窗口的第一个元素 放到状态当中
                mapState.put(windowInfo, list);
            } else {
                //存在数据 则 直接将数据加入到窗口中就可以了.
                mapState.get(windowInfo).add(in);
            }

            //注册窗口(结束的时间-1ms)的定时器
            ctx.timerService().registerProcessingTimeTimer(windowEnd - 1);
        }

        @Override
        public void onTimer(long timeTs, OnTimerContext ctx, Collector<UserViewCountPreWindow> out) throws Exception {
            //还原窗口的信息 起始时间 结束时间 以及 windowInfo
            long windowEnd = timeTs + 1;
            long windowStart = windowEnd - windowSize;

            Tuple2<Long, Long> windowInfo = Tuple2.of(windowStart, windowEnd);

            //得到当前的key 也就是我们的userName
            String username = ctx.getCurrentKey();

            //得到当前窗口的长度 也就是我们要统计的值
            long count = mapState.get(windowInfo).size();

            out.collect(new UserViewCountPreWindow(username, count, windowStart, windowEnd));

            //销毁窗口
            mapState.remove(windowInfo);
        }
    }
}
