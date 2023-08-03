package com.flink.checkpoint;

import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @Author wangziyu1
 * @Date 2022/11/21 10:42
 * @Version 1.0
 *
 * Flink故障恢复机制的核心，就是应用状态的一致性检查点。
 * 有状态流应用的一致检查点，其实就是所有并行子任务的状态，在某个时间点的一份拷贝（一份快照）。
 * 上游是一个可重置读取位置的持久化设备（例如Kafka）。
 */
public class CheckPointDemo2 {

    public static void main(String[] args) throws Exception {


        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        env.fromElements(1, 2, 3, 4, 5)
                .keyBy(r -> r % 2)
                .reduce(new ReduceFunction<Integer>() {
                    @Override
                    public Integer reduce(Integer v1, Integer v2) throws Exception {
                        return v1 + v2;
                    }
                })
                .print();

        env.execute();
    }
}
