package com.flink.api.transform;

import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @version 1.0
 * @Author wangziyu1
 * @Date 9/17/21 2:34 PM
 * @注释
 */
public class Flink01_TransForm_KeyBy_Function {

    public static void main(String[] args) throws Exception {


        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

//       // 奇数分一组, 偶数分一组

        //匿名类的写法
   /*     env.fromElements(1, 2, 3, 4, 5,10)
                .keyBy(new KeySelector<Integer, String>() {
                    @Override
                    public String getKey(Integer value) throws Exception {

                        return value % 2 == 0 ? "偶数" : "奇数";
                    }
                }).print();*/

        // lambda 写法
        env
                .fromElements(10, 3, 5, 9, 20, 8)
                .keyBy(value -> value % 2 == 0 ? "偶数" : "奇数")
                .print();



        env.execute();


    }
}
