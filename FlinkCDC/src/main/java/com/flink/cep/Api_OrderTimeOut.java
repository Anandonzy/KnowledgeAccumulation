package com.flink.cep;

import lombok.*;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternFlatSelectFunction;
import org.apache.flink.cep.PatternFlatTimeoutFunction;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.util.List;
import java.util.Map;

/**
 * @Author wangziyu1
 * @Date 2022/12/6 15:56
 * @Version 1.0
 */
public class Api_OrderTimeOut {


    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);

        DataStreamSource<Event> stream = env.addSource(new SourceFunction<Event>() {

            @Override
            public void run(SourceContext<Event> ctx) throws Exception {
                ctx.collectWithTimestamp(new Event("order-1", "create", 1000L), 1000L);
                ctx.collectWithTimestamp(new Event("order-2", "create", 2000L), 2000L);
                ctx.collectWithTimestamp(new Event("order-1", "pay", 3000L), 3000L);

            }

            @Override
            public void cancel() {

            }
        });
        stream.keyBy(r -> r.key)
                .process(new KeyedProcessFunction<String, Event, String>() {

                    private ValueState<Event> state;


                    @Override
                    public void open(Configuration parameters) throws Exception {
                        state = getRuntimeContext().getState(new ValueStateDescriptor<Event>("state", Types.POJO(Event.class)));
                    }

                    @Override
                    public void onTimer(long timerTs, OnTimerContext ctx, Collector<String> out) throws Exception {
                        //定时器触发
                        if (state.value() != null && state.value().equals("create")) {
                            out.collect(state.value().key + "超时未支付~");
                        }

                    }

                    @Override
                    public void processElement(Event event, Context ctx, Collector<String> out) throws Exception {

                        if (event.value.equals("create")) {
                            state.update(event);
                            //注册五秒之后的定时器
                            ctx.timerService().registerEventTimeTimer(event.ts + 5000L);
                        } else if (event.value.equals("pay")) {
                            out.collect(event.key + "正常支付");
                            state.clear(); //删除包村的create事件
                        }
                    }
                })
                .print();


        env.execute();


    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class Event {

        public String key;
        public String value;
        public Long ts;
    }

}
