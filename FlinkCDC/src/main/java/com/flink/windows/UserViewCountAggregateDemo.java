package com.flink.windows;

import com.flink.api.soruce.ClickSource;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

/**
 * @Author wangziyu1
 * @Date 2022/11/8 13:33
 * @Version 1.0
 */
public class UserViewCountAggregateDemo {


    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);
        env.addSource(new ClickSource())
                .keyBy(ClickSource.Click::getName)
                .window(TumblingProcessingTimeWindows.of(Time.seconds(10)))
                //当AggregateFunction 和 ProcessWindowFunction 结合使用的时候需要调用 AggregateFunction
                .aggregate(new AggregateResult(),
                        new WindowResult())
                .print();


        env.execute("UserViewCountDemo");


    }

    private static class AggregateResult implements AggregateFunction<ClickSource.Click, Long, Long> {
        //创建窗口的时候初始化一个累加器
        @Override
        public Long createAccumulator() {
            return 0L;
        }

        /**
         * 每来一条数据 累加器加1
         *
         * @param in
         * @param acc
         * @return
         */
        @Override
        public Long add(ClickSource.Click in, Long acc) {
            return acc + 1L;
        }

        /**
         * 将累加器的结果向下发送
         *
         * @param acc
         * @return
         */
        @Override
        public Long getResult(Long acc) {
            return acc;
        }

        /**
         * 当不使用eventTime 的时候暂时不用写
         *  会话窗口的时候才会调用
         * @param acc1
         * @param acc2
         * @return
         */
        @Override
        public Long merge(Long acc1, Long acc2) {
            return null;
        }
    }

    //输入泛型是AggregateFunction最终闭合窗口调用getResult之后的类型 是 long
    private static class WindowResult extends ProcessWindowFunction<Long, UserViewCountPreWindow, String, TimeWindow> {
        @Override
        public void process(String key, Context ctx, Iterable<Long> elements, Collector<UserViewCountPreWindow> out) throws Exception {
            //这里注意 elements 里面就只有一个元素了 就是上游AggregateFunction 里面的getResult 方法返回的结果
            out.collect(new UserViewCountPreWindow(key,
                    //取出来唯一一个元素也就是累计计算的结果
                    elements.iterator().next(),
                    ctx.window().getStart(),
                    ctx.window().getEnd()));
        }
    }
}
