package com.flink.cep;

import lombok.*;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.util.HashMap;

/**
 * @Author wangziyu1
 * @Date 2022/12/6 16:38
 * @Version 1.0
 * CEP底层原理 有限状态机
 */
public class NFADemo {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        env
                .fromElements(
                        new Event("user-1", "fail", 1000L),
                        new Event("user-1", "fail", 2000L),
                        new Event("user-2", "success", 3000L),
                        new Event("user-1", "fail", 4000L),
                        new Event("user-1", "fail", 5000L)

                )
                .keyBy(r -> r.key)
                .process(new StateMachine())
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

    private static class StateMachine extends KeyedProcessFunction<String, Event, String> {

        private HashMap<Tuple2<String, String>, String> stateMachine = new HashMap<>();
        private ValueState<String> currentState;

        @Override

        public void open(Configuration parameters) throws Exception {
            //INITIAL 状态接收到fial事件 跳转到S1事件
            stateMachine.put(Tuple2.of("INITIAL", "fail"), "S1");
            stateMachine.put(Tuple2.of("INITIAL", "success"), "SUCCESS");
            stateMachine.put(Tuple2.of("S1", "fail"), "S2");
            stateMachine.put(Tuple2.of("S1", "success"), "SUCCESS");
            stateMachine.put(Tuple2.of("S2", "fail"), "FAIL");
            stateMachine.put(Tuple2.of("S2", "success"), "success");
            currentState = getRuntimeContext().getState(new ValueStateDescriptor<String>("current-state", Types.STRING));
        }

        @Override
        public void processElement(Event event, Context ctx, Collector<String> out) throws Exception {

            if (currentState.value() == null) {
                //到达的第一条数据
                currentState.update("INITIAL");
            }
            //计算将要跳转的状态
            String nextState = stateMachine.get(Tuple2.of(currentState.value(), event.value));

            if (nextState.equals("FAIL")) {
                out.collect(event.key + "连续三次登陆失败~");
                //重置到S2状态
                currentState.update("S2");
            } else if (nextState.equals("SUCCESS")) {
                currentState.update("INITIAL");
            } else {
                currentState.update(nextState);
            }

        }
    }
}
