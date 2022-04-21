package com.flink.api.transform;


import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @version 1.0
 * @Author wangziyu1
 * @Date 9/17/21 11:11 AM
 * @注释
 */
public class Flink01_TransForm_Map_RichMapFunction {


    public static void main(String[] args) throws Exception{

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment().setParallelism(5);

        env
                .fromElements(1, 2, 3, 4, 5)
                .map(new MyRichMapFunction()).setParallelism(2)
                .print();

        env.execute();

    }

    public static class MyRichMapFunction extends RichMapFunction<Integer, Integer> {

        // 默认生命周期方法, 初始化方法, 在每个并行度上只会被调用一次
        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            System.out.println("open ... 执行一次");
        }

        @Override
        public void close() throws Exception {
            System.out.println("close ... 执行一次");
            super.close();
        }

        @Override
        public Integer map(Integer v) throws Exception {
            return v * v;
        }


    }


}
