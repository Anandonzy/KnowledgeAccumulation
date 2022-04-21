package com.flink.api.transform;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @version 1.0
 * @Author wangziyu1
 * @Date 9/17/21 2:22 PM
 * @注释
 */
public class Flink01_TransForm_Fliter_Function {


    public static void main(String[] args) throws Exception {


        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

//        保留偶数, 舍弃奇数

        //匿名类的写法
    /*    env.fromElements(1, 2, 3, 4, 5)
                .filter(new FilterFunction<Integer>() {
                    @Override
                    public boolean filter(Integer v) throws Exception {
                        return v % 2 == 0;
                    }
                }).print();*/

    // lambda 写法
        env.fromElements(1, 2, 3, 4, 5)
                .filter(v -> v % 2 == 0).print();
        env.execute();


    }
}
