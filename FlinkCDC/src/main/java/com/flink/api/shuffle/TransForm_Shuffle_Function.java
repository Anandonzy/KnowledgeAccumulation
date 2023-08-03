package com.flink.api.shuffle;

import org.apache.flink.api.common.functions.Partitioner;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;

/**
 * @Author wangziyu1
 * @Date 2022/11/3 17:50
 * @Version 1.0
 * 物理分区算子:
 * 将数据分发到不同的并行子任务。
 * 常用的也就是 broadcast
 * shuffle()：随机向下游的并行子任务发送数据。
 * rebalance()：将数据轮询发送到下游的所有并行子任务中。
 * rescale()：将数据轮询发送到下游的部分并行子任务中。用在下游算子的并行度是上游算子的并行度的整数倍的情况。
 * broadcast()：将数据广播到下游的所有并行子任务中。
 * global()：将数据发送到下游的第一个（索引为0）并行子任务中。
 * custom()：自定义分区。可以自定义将某个key的数据发送到下游的哪一个并行子任务中去。
 */
public class TransForm_Shuffle_Function {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        /**
         * shuffle()：随机向下游的并行子任务发送数据。
         */
        //env.
        //        fromElements(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        //        .setParallelism(1)
        //        .shuffle()
        //        .print("当前并行度:")
        //        .setParallelism(4);


        /**
         * rebalance() 将数据轮询发送到下游的所有并行子任务中。
         */
        //env.
        //        fromElements(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        //        .setParallelism(1)
        //        .rebalance()
        //        .print("当前并行度:")
        //        .setParallelism(4);

        /**
         *  broadcast()：将数据广播到下游的所有并行子任务中。 每一个并行度都有一份数据
         */
        //env.
        //        fromElements(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        //        .setParallelism(1)
        //        .broadcast()
        //        .print("当前并行度:")
        //        .setParallelism(4);


        /**
         * global 将数据发送到下游的第一个（索引为0）并行子任务中。
         */
        //env.
        //        fromElements(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        //        .setParallelism(1)
        //        .global()
        //        .print("当前并行度:")
        //        .setParallelism(4);

        /**
         * custom()：自定义分区。可以自定义将某个key的数据发送到下游的哪一个并行子任务中去。
         */
        //env.
        //        fromElements(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        //        .setParallelism(1)
        //        .partitionCustom(new Partitioner<Integer>() {
        //            @Override
        //            public int partition(Integer value, int key) {
        //                //将key为0或者1 的都发送到partition为0 的分区里面
        //                if (key == 0 || key == 1) {
        //                    return 0;
        //
        //                } else {
        //                    return 1;
        //                }
        //            }
        //
        //        }, new KeySelector<Integer, Integer>() {
        //            /**
        //             *
        //             * @param value
        //             * @return
        //             * @throws Exception
        //             */
        //            @Override
        //            public Integer getKey(Integer value) throws Exception {
        //                return value % 3;
        //            }
        //        })
        //        .print("当前并行度:")
        //        .setParallelism(4);
        /**
         * rescale()：将数据轮询发送到下游的部分并行子任务中。用在下游算子的并行度是上游算子的并行度的整数倍的情况。
         * 1 3 5 7 发到两个并行度上
         * 2 4 6 8 发到另外两个并行度上
         * 如果是 rebalance 是轮询的发到每一个并行度上
         */

        env.addSource(new RichParallelSourceFunction<Integer>() {
                    @Override
                    public void run(SourceContext<Integer> ctx) throws Exception {

                        //不同的并行子任务发送不同的数据
                        for (int i = 1; i < 9; i++) {
                            if (i % 2 == getRuntimeContext().getIndexOfThisSubtask()) {
                                ctx.collect(i);
                            }
                        }
                    }

                    @Override
                    public void cancel() {

                    }
                })
                .setParallelism(2)
                .rescale()
                .print().setParallelism(4);


        env.execute();
    }
}
