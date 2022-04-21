package com.flink.state;


import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @version 1.0
 * @Author wangziyu1
 * @Date 9/27/21 2:50 PM
 * @注释 keystat 一个并行度一个状态
 */
public class Flink01_State_Operator {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment
                .getExecutionEnvironment()
                .setParallelism(3);
        env
                .socketTextStream("localhost", 9999)
                .map(new MyCountMapper())
                .print();

        env.execute();
    }

    private static class MyCountMapper implements MapFunction<String, Long>, CheckpointedFunction {


        private Long count = 0L;
        private ListState<Long> state;


        @Override
        public Long map(String s) throws Exception {
            count++;
            return count;
        }

        // Checkpoint时会调用这个方法，我们要实现具体的snapshot逻辑，比如将哪些本地状态持久化
        @Override
        public void snapshotState(FunctionSnapshotContext functionSnapshotContext) throws Exception {
            System.out.println("snapshotState...");
            state.clear();
            state.add(count);
        }


        @Override
        public void initializeState(FunctionInitializationContext context) throws Exception {
            System.out.println("initializeState...");
            state = context
                    .getOperatorStateStore()
                    .getListState(new ListStateDescriptor<Long>("state", Long.class));
            for (Long c : state.get()) {
                count += c;
            }

        }
    }

}
