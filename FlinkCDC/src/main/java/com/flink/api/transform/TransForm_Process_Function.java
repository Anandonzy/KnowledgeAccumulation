package com.flink.api.transform;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

/**
 * @Author wangziyu1
 * @Date 2022/11/3 23:29
 * @Version 1.0
 * 使用ProcessFunction实现flatMap的功能
 */
public class TransForm_Process_Function {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.fromElements("aa", "bb", "cc")
                .process(new ProcessFunction<String, String>() {
                    /**
                     * 来一条处理一条
                     * @param in
                     * @param ctx
                     * @param out
                     * @throws Exception
                     */
                    @Override
                    public void processElement(String in, ProcessFunction<String, String>.Context ctx, Collector<String> out) throws Exception {
                        if (in.equals("aa")) {
                            out.collect(in);
                        } else if ("bb".equals(in)){
                            out.collect(in);
                            out.collect(in);
                        }
                    }
                })

                .print().setParallelism(2);
        env.execute();
    }
}
