package com.flink.api.transform;


import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * @version 1.0
 * @Author wangziyu1
 * @Date 9/17/21 11:21 AM
 * @注释
 */
public class Flink01_TransForm_FlatMap_Function {

    public static void main(String[] args) throws Exception{

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

// 新的流存储每个元素的平方和3次方
/*
        env
                .fromElements(1, 2, 3, 4, 5)
                .flatMap(new FlatMapFunction<Integer, Integer>() {
                    @Override
                    public void flatMap(Integer value, Collector<Integer> out) throws Exception {
                        out.collect(value * value);
                        out.collect(value * value * value);
                    }
                })
                .print();
*/


        env
                .fromElements(1, 2, 3, 4, 5)
                .flatMap((Integer value, Collector<Integer> out) -> {
                    out.collect(value * value);
                    out.collect(value * value * value);
                }).returns(Types.INT)
                .print();


        env.execute();


    }
}
