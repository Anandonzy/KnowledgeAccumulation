package com.study.wc;

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
 * @Date 2022/11/15 16:26
 * @Version 1.0
 * 触发器demo练习
 * 根据输入数据的时间 接下来每个整数秒出发一次调用
 */
public class TriggerWhyImproveDemo {


    public static void main(String[] args) throws Exception {


        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        env.addSource(new ClickSource())
                //forMonotonousTimestamps 语法糖 不设置水位线的延迟时间.
                .assignTimestampsAndWatermarks(WatermarkStrategy.<ClickSource.Click>forMonotonousTimestamps().withTimestampAssigner(new SerializableTimestampAssigner<ClickSource.Click>() {
                    @Override
                    public long extractTimestamp(ClickSource.Click click, long l) {
                        return click.getTs();
                    }
                }))
                .keyBy(ClickSource.Click::getName)
                .window(TumblingEventTimeWindows.of(Time.hours(12)))
                .trigger(new Trigger<ClickSource.Click, TimeWindow>() {
                    @Override
                    public TriggerResult onElement(ClickSource.Click in, long l, TimeWindow window, TriggerContext ctx) throws Exception {

                        //利用状态保存是否是窗口的第一条数据
                        ValueState<Boolean> flag = ctx.getPartitionedState(new ValueStateDescriptor<Boolean>("flag"
                                , Types.BOOLEAN));

                        if (flag.value() == null) {
                            //证明是第一条数据
                            long nextSecond = (in.getTs() + 1000L) - (in.getTs() % 1000L);

                            //注册定时器 接下里的 「整数秒」 的定时器
                            ctx.registerEventTimeTimer(nextSecond);

                            //修改标志位
                            flag.update(true);
                        }
                        return TriggerResult.CONTINUE;
                    }

                    @Override
                    public TriggerResult onProcessingTime(long l, TimeWindow timewindowWindow, TriggerContext ctx) throws Exception {
                        return null;
                    }

                    @Override
                    public TriggerResult onEventTime(long timerTs, TimeWindow window, TriggerContext ctx) throws Exception {

                        //如果定时器的时间小于窗口的结束时间 则注册下一个「整数秒」 的定时器
                        if (timerTs < window.getEnd()) {
                            if (timerTs + 1000L < window.getEnd()) {
                                ctx.registerEventTimeTimer(timerTs + 1000L);
                            }
                            return TriggerResult.FIRE;

                        }
                        return TriggerResult.CONTINUE;
                    }

                    @Override
                    public void clear(TimeWindow window, TriggerContext ctx) throws Exception {
                        ValueState<Boolean> flag = ctx.getPartitionedState(new ValueStateDescriptor<Boolean>("flag"
                                , Types.BOOLEAN));

                        flag.clear();
                    }
                })
                .aggregate(new AggregateFunction<ClickSource.Click, Long, Long>() {
                    @Override
                    public Long createAccumulator() {
                        return 0L;
                    }

                    @Override
                    public Long add(ClickSource.Click in, Long acc) {
                        return acc + 1L;
                    }

                    @Override
                    public Long getResult(Long acc) {
                        return acc;
                    }

                    @Override
                    public Long merge(Long aLong, Long acc1) {
                        return null;
                    }
                }, new ProcessWindowFunction<Long, ProductUvWindow, String, TimeWindow>() {
                    @Override
                    public void process(String key, Context ctx, Iterable<Long> element, Collector<ProductUvWindow> out) throws Exception {
                        out.collect(new ProductUvWindow(key, element.iterator().next(), ctx.window().getStart(), ctx.window().getEnd()));
                    }
                })
                .print();


        env.execute();

    }
}
