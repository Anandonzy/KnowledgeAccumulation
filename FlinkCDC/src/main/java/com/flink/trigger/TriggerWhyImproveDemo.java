package com.flink.trigger;

import com.flink.api.soruce.ClickSource;
import com.flink.bean.ProductUvWindow;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.triggers.TriggerResult;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

/**
 * @Author wangziyu1
 * @Date 2022/11/15 15:51
 * @Version 1.0
 * 基于 TriggerWhyDemo 引入触发器解决不能出结果的问题
 */
public class TriggerWhyImproveDemo {


    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);

        //这个地方是文件 如果是无界数据流就不会输出数据.就要等窗口输出.
        env.addSource(new ClickSource())
                .assignTimestampsAndWatermarks(WatermarkStrategy.<ClickSource.Click>forMonotonousTimestamps().withTimestampAssigner(new SerializableTimestampAssigner<ClickSource.Click>() {
                    @Override
                    public long extractTimestamp(ClickSource.Click click, long l) {
                        return click.getTs();
                    }
                }))
                .keyBy(ClickSource.Click::getName)
                .window(TumblingEventTimeWindows.of(Time.hours(10)))
                .trigger(new Trigger<ClickSource.Click, TimeWindow>() {
                    @Override
                    public TriggerResult onElement(ClickSource.Click in, long l, TimeWindow timeWindow, TriggerContext ctx) throws Exception {
                        //每来一条数据 调用一下下游的aggregate算子的结果
                        //这里实现的业务 窗口内的第一条数据的时间戳 接下来所有的「整数秒」 都触发一次计算.

                        //用来保存是否是第一条状态.
                        //下面的状态是每一个窗口内独有的.
                        ValueState<Boolean> flag = ctx.getPartitionedState(
                                new ValueStateDescriptor<Boolean>("flag", Types.BOOLEAN));

                        if (flag.value() == null) { //这个地方注册定时器 只是第一条数据来的注册的下个整数秒的定时器 后面需要判断时间+1000L 是否小于窗口时间再继续注册.
                            //到达窗口的第一条数据
                            //计算第一条数据的接下来的「整数秒」.
                            //1234ms ->  1234+1000 - 1234%1000 =2000
                            long nextSecond = in.getTs() + 1000L - in.getTs() % 1000L;

                            //注册事件时间定时器
                            ctx.registerEventTimeTimer(nextSecond);

                            //将标志位置位true
                            flag.update(true);
                        }
                        //窗口不做任何事情
                        return TriggerResult.CONTINUE;

                    }

                    @Override
                    public TriggerResult onProcessingTime(long l, TimeWindow timeWindow, TriggerContext ctx) throws Exception {
                        return null;
                    }

                    @Override
                    public TriggerResult onEventTime(long timerTs, TimeWindow window, TriggerContext ctx) throws Exception {
                        //如果当前定时器的时间小于 窗口的结束时间 可以触发一次
                        if (timerTs < window.getEnd()) {
                            if (timerTs + 1000L < window.getEnd()) {
                                //还需要再注册下个 [整数秒] 的定时器
                                ctx.registerEventTimeTimer(timerTs + 1000L);
                            }
                            return TriggerResult.FIRE;
                        }
                        return TriggerResult.CONTINUE;
                    }

                    @Override
                    public void clear(TimeWindow timeWindow, TriggerContext ctx) throws Exception {
                        //下面的状态是每一个窗口内独有的. 这个状态变量是单例的 所以也要在窗口闭合的时候清空状态
                        ValueState<Boolean> flag = ctx.getPartitionedState(
                                new ValueStateDescriptor<Boolean>("flag", Types.BOOLEAN));
                        //
                        flag.clear();
                    }
                })
                .aggregate(
                        new AggregateFunction<ClickSource.Click, Long, Long>() {
                            @Override
                            public Long createAccumulator() {
                                return 0L;
                            }

                            @Override
                            public Long add(ClickSource.Click value, Long accumulator) {
                                return accumulator + 1L;
                            }

                            @Override
                            public Long getResult(Long accumulator) {
                                return accumulator;
                            }

                            @Override
                            public Long merge(Long a, Long b) {
                                return null;
                            }
                        },
                        new ProcessWindowFunction<Long, ProductUvWindow, String, TimeWindow>() {
                            @Override
                            public void process(String key, Context ctx, Iterable<Long> iterable, Collector<ProductUvWindow> out) throws Exception {
                                out.collect(new ProductUvWindow(
                                        key,
                                        iterable.iterator().next(),
                                        ctx.window().getStart(),
                                        ctx.window().getEnd()
                                ));
                            }
                        }
                )
                .print();
        env.execute();


    }
}
