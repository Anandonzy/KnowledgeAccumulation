package com.flink.api.transform;


import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;


/**
 * @version 1.0
 * @Author wangziyu1
 * @Date 9/17/21 11:01 AM
 * @注释 参数
 * lambda表达式或MapFunction实现类
 * 返回
 * DataStream → DataStream
 * 示例
 * 得到一个新的数据流: 新的流的元素是原来流的元素的平方
 */
public class Flink01_TransForm_Map_Anonymous {

    /**
     * 匿名内部类对象
     */

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();


        /**
         * 匿名内部类对象
         */
/*        env
                .fromElements(1, 2, 3, 4, 5)
                .map(new MapFunction<Integer, Integer>() {
                    @Override
                    public Integer map(Integer value) throws Exception {
                        return value * value;
                    }
                }).print();*/

        /**
         * 	Lambda表达式表达式
         */

     /*   env.fromElements(1, 2, 3, 4, 5)
                .map(v -> v * v)
                .print();*/

        /**
         * 	静态内部类
         */

        env
                .fromElements(1, 2, 3, 4, 5)
                .map(new MyMapFunction())
                .print();


        env.execute();

    }

    public static class MyMapFunction implements MapFunction<Integer, Integer> {

        @Override
        public Integer map(Integer v) throws Exception {
            return v * v;
        }
    }
}
