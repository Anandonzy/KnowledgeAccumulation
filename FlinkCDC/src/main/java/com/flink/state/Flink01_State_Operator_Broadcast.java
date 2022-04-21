package com.flink.state;

import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ReadOnlyBroadcastState;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.util.Collector;

/**
 * @version 1.0
 * @Author wangziyu1
 * @Date 9/27/21 3:49 PM
 * @注释
 */
public class Flink01_State_Operator_Broadcast {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment
                .getExecutionEnvironment()
                .setParallelism(3);
        DataStreamSource<String> dataStream = env.socketTextStream("localhost", 9999);
        DataStreamSource<String> controlStream = env.socketTextStream("localhost", 8888);

        MapStateDescriptor<String, String> stateDescriptor = new MapStateDescriptor<>("state", String.class, String.class);

        // 广播流
        BroadcastStream<String> broadcastStream = controlStream.broadcast(stateDescriptor);

        dataStream.connect(broadcastStream)
                .process(new BroadcastProcessFunction<String, String, String>() {
                    @Override
                    public void processElement(String value, ReadOnlyContext ctx, Collector<String> out) throws Exception {

                        // 从广播状态中取值, 不同的值做不同的业务
                        ReadOnlyBroadcastState<String, String> state = ctx.getBroadcastState(stateDescriptor);

                        if ("1".equals(state.get("switch"))) {
                            out.collect("切换到1号配置....");
                        } else if ("0".equals(state.get("switch"))) {
                            out.collect("切换到0号配置....");
                        } else {
                            out.collect("切换到其他配置....");
                        }
                    }

                    @Override
                    public void processBroadcastElement(String value, Context ctx, Collector<String> collector) throws Exception {

                        BroadcastState<String, String> state = ctx.getBroadcastState(stateDescriptor);
                        // 把值放入广播状态
                        state.put("switch", value);
                    }
                }).print();

        env.execute();


    }
}
