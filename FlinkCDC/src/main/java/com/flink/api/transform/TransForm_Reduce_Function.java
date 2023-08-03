package com.flink.api.transform;

import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @Author wangziyu1
 * @Date 2022/11/2 16:28
 * @Version 1.0
 */
public class TransForm_Reduce_Function {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env
                .fromElements(1, 2, 3, 4, 5, 6, 7, 8)
                .setParallelism(1)
                .keyBy(r -> r % 3)
                .reduce(new ReduceFunction<Integer>() {
                    @Override
                    public Integer reduce(Integer v1, Integer acc) throws Exception {
                        return v1 + acc;
                    }
                })
                .setParallelism(4)
                .print()
                .setParallelism(4);


        env.execute();


    }
}
