package com.flink.trigger;

import com.flink.api.soruce.ClickSource;
import com.flink.bean.ProductUvWindow;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

/**
 * @Author wangziyu1
 * @Date 2022/11/15 14:49
 * @Version 1.0
 * 10个小时的窗口 如果是无界数据流 就会等数据达到10h才会输出.
 * 展示为啥要有触发器 如果没有想要看到中间的结果就只能等 ,但是如果有触发器 就可以提前看到聚合的结果.
 */
public class TriggerWhyDemo {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);

        //这个地方是文件 如果是无界数据流就不会输出数据.就要等窗口事出.
        env.addSource(new ClickSource())
                .assignTimestampsAndWatermarks(WatermarkStrategy.<ClickSource.Click>forMonotonousTimestamps().withTimestampAssigner(new SerializableTimestampAssigner<ClickSource.Click>() {
                    @Override
                    public long extractTimestamp(ClickSource.Click click, long l) {
                        return click.getTs();
                    }
                }))
                .keyBy(ClickSource.Click ::getName)
                .window(TumblingEventTimeWindows.of(Time.hours(10)))
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
