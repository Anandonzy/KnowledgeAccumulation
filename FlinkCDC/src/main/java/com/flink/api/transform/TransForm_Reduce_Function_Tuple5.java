package com.flink.api.transform;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.Random;

/**
 * @Author wangziyu1
 * @Date 2022/11/2 17:13
 * @Version 1.0
 * 根据输入数据求出 最大值 最小值 总和 总条数 平均值
 */
public class TransForm_Reduce_Function_Tuple5 {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.addSource(new SourceFunction<Integer>() {
                    private boolean flag = true;
                    private Random random = new Random();

                    @Override
                    public void run(SourceContext<Integer> sourceContext) throws Exception {
                        while (flag) {
                            int value = random.nextInt(10);
                            sourceContext.collect(value);
                            Thread.sleep(1000L);
                        }
                    }

                    @Override
                    public void cancel() {
                        flag = false;
                    }
                }).map(r -> Tuple5.of(r, r, r, 1, r))
                .returns(Types.TUPLE(Types.INT, Types.INT, Types.INT, Types.INT, Types.INT))
                .keyBy(r -> "int")
                .reduce(new ReduceFunction<Tuple5<Integer, Integer, Integer, Integer, Integer>>() {
                    @Override
                    public Tuple5<Integer, Integer, Integer, Integer, Integer> reduce(Tuple5<Integer, Integer, Integer, Integer, Integer> in, Tuple5<Integer, Integer, Integer, Integer, Integer> acc) throws Exception {
                        return Tuple5.of(
                                Math.max(in.f0, acc.f0), //最大值
                                Math.min(in.f1, acc.f1), //最小值
                                in.f2 + acc.f2, //求和
                                in.f3 + acc.f3, // 条数
                                in.f2 + acc.f2 / (in.f3 + acc.f3) //平均值
                        );
                    }
                }).print();

        env.execute();

    }
}
