package com.flink.api.transform;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * @Author wangziyu1
 * @Date 2022/11/3 22:57
 * @Version 1.0
 */
public class TransForm_RichFlatMap_Function {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();


        env.fromElements(1, 2, 3, 4).setParallelism(1)
                .flatMap(new RichFlatMapFunction<Integer, String>() {


                    @Override
                    public void open(Configuration parameters) throws Exception {
                        System.out.println("flatMap的并行子任务：" +
                                getRuntimeContext().getIndexOfThisSubtask() + " " + "声明周期开始");
                    }

                    @Override
                    public void flatMap(Integer in, Collector<String> out) throws Exception {
                        out.collect("flatMap的并行子任务的索引是:" + getRuntimeContext().getIndexOfThisSubtask() +
                                "处理的数据是:" + in);

                    }

                    @Override
                    public void close() throws Exception {
                        System.out.println("flatMap的并行子任务：" +
                                getRuntimeContext().getIndexOfThisSubtask() + " " + "声明周期结束~~~");
                    }
                }).setParallelism(2)
                .print().setParallelism(2);


        env.execute();


    }
}
