package com.flink.api.soruce;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

/**
 * @Author wangziyu1
 * @Date 2022/11/3 23:11
 * @Version 1.0
 * 并行数据源 可以设置并行度
 * 每一个并行子任务都会向下游发送同样的run 方法的内容
 */
public class Source_Parallel {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //这个里要注意的是RichParallelSourceFunction 而不是RichSourceFunction
        //RichSourceFunction 不能设置并行度
        env.addSource(new RichParallelSourceFunction<String>() {
                    @Override
                    public void run(SourceContext<String> ctx) throws Exception {
                        for (int i = 0; i < 5; i++) {
                            ctx.collect("source的并行子任务:" +
                                    getRuntimeContext().getIndexOfThisSubtask()
                                    + "发送到数据是:" + i);
                        }
                    }

                    @Override
                    public void cancel() {

                    }
                }).setParallelism(2)
                .print().setParallelism(2);

        env.execute();


    }


}
