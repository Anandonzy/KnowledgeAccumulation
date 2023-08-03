package com.flink.api.transform;


import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import java.sql.Timestamp;

/**
 * @Author wangziyu1
 * @Date 2022/11/3 23:34
 * @Version 1.0
 * keyProcessFunction
 * 基于keyBy之后的监控流的keyByProcessFunction
 * 定时器
 * 针对keyBy之后的键控流（KeyedStream），可以使用KeyedProcessFunction
 * <p>
 * KeyedProcessFunction<KEY, IN, OUT>：KEY是key的泛型，IN是输入的泛型，OUT是输出的泛型。
 * processElement：来一条数据，触发调用一次。
 * onTimer：定时器。时间到达某一个时间戳触发调用。
 * <p>
 * 每个key都会维护自己的定时器，每个key都只能访问自己的定时器。就好像每个key都只能访问自己的累加器一样。
 * <p>
 * 针对每个key，在某个时间戳只能注册一个定时器，定时器不能重复注册，如果某个时间戳已经注册了定时器，那么再对这个时间戳注册定时器就不起作用了。
 * <p>
 * .registerProcessingTimeTimer(ts)：在机器时间戳ts注册了一个定时器（onTimer）。
 * <p>
 * 维护的内部状态
 * <p>
 * 状态变量
 * 定时器
 * processElement方法和onTimer方法：这两个方法是原子性的，无法并发执行。某个时刻只能执行一个方法。因为这两个方法都有可能操作相同的状态变量。例如：到达了一个事件，此时onTimer正在执行，则必须等待onTimer执行完以后，再调用processElement。再比如：到达了一个水位线，想触发onTimer，但此时processElement正在执行，那么必须等待processElement执行完以后再执行onTimer。
 * <p>
 * 当水位线到达KeyedProcessFunction，如果这条水位线触发了onTimer的执行，则必须等待onTimer执行完以后，水位线才能向下游发送。
 * <p>
 * 当水位线到达ProcessWindowFunction，如果这条水位线触发了process方法的执行，则必须等待process方法执行完以后，水位线才能向下游发送。
 * <p>
 * 📝在KeyedProcessFunction中，可以认为维护了多张HashMap，每个状态变量的定义都会初始化一张HashMap，同时还有一张维护每个key的定时器队列的HashMap。
 */
public class TransForm_Key_Process_Function {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        env.socketTextStream("localhost", 9999)
                .keyBy(r -> r)
                .process(new KeyedProcessFunction<String, String, String>() {
                    @Override
                    public void processElement(String in, KeyedProcessFunction<String, String, String>.Context ctx, Collector<String> out) throws Exception {

                        //当前机器的时间
                        long currTs = ctx.timerService().currentProcessingTime();

                        //30s 之后的时间
                        long thirtySecond = currTs + 30 * 1000L;

                        //60s 之后的时间
                        long sixtySecond = currTs + 60 * 1000L;

                        //注册定时器
                        ctx.timerService().registerProcessingTimeTimer(thirtySecond);
                        ctx.timerService().registerProcessingTimeTimer(sixtySecond);

                        out.collect("key 为" + ctx.getCurrentKey() + ",数据: " + in + " 到达的时间是: " + new Timestamp(currTs) + "" +
                                "注册第一个定时器的时间是: " + new Timestamp(thirtySecond) + "" +
                                "注册第二个定时器的时间是: " + new Timestamp(sixtySecond));
                    }

                    @Override
                    public void onTimer(long timeTs, KeyedProcessFunction<String, String, String>.OnTimerContext ctx, Collector<String> out) throws Exception {

                        out.collect("key 为" + ctx.getCurrentKey() + "的定时器被触发了," +
                                "定时器的时间戳是:" + new Timestamp(timeTs) + ";" +
                                "定时器执行的时间戳是: " + new Timestamp(ctx.timerService().currentProcessingTime()));


                    }
                })
                .print();

        env.execute();

    }

}
