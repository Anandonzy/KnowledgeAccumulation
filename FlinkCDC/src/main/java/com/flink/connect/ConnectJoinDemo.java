package com.flink.connect;

import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.util.Collector;

/**
 * @Author wangziyu1
 * @Date 2022/11/14 18:36
 * @Version 1.0
 * 实现内连接查询
 * SELECT * FROM A JOIN B ON A.key=B.key;
 * 底层api 使用的是ListState
 */
public class ConnectJoinDemo {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);

        DataStreamSource<Tuple2<String, String>> stream1 = env.fromElements(
                Tuple2.of("a", "left-1")
                , Tuple2.of("b", "left-1")
                , Tuple2.of("a", "left-2"));

        DataStreamSource<Tuple2<String, String>> stream2 = env.fromElements(
                Tuple2.of("a", "right-1")
                , Tuple2.of("b", "right-1")
                , Tuple2.of("b", "right-2"));

        stream1.keyBy(r -> r.f0)
                .connect(stream2.keyBy(r -> r.f0))
                .process(new InnerJoin())
                .print();


        env.execute();


    }

    private static class InnerJoin extends CoProcessFunction<Tuple2<String, String>, Tuple2<String, String>, String> {

        private ListState<Tuple2<String, String>> history1;
        private ListState<Tuple2<String, String>> history2;

        @Override
        public void open(Configuration parameters) throws Exception {
            history1 = getRuntimeContext().getListState(new ListStateDescriptor<Tuple2<String, String>>("history1",
                    Types.TUPLE(Types.STRING, Types.STRING)));

            history2 = getRuntimeContext().getListState(new ListStateDescriptor<Tuple2<String, String>>("history2",
                    Types.TUPLE(Types.STRING, Types.STRING)));
        }

        @Override
        public void processElement1(Tuple2<String, String> in1, Context ctx, Collector<String> out) throws Exception {

            history1.add(in1);

            for (Tuple2<String, String> right : history2.get()) {
                out.collect(in1 + "->" + right);
            }


        }

        @Override
        public void processElement2(Tuple2<String, String> in2, Context ctx, Collector<String> out) throws Exception {
            history2.add(in2);

            for (Tuple2<String, String> left : history1.get()) {
                out.collect(in2 + "->" + left);
            }

        }
    }
}
