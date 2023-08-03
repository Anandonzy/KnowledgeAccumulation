package com.flink.state;

import com.flink.api.soruce.ClickSource;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

/**
 * @Author wangziyu1
 * @Date 2022/11/7 17:52
 * @Version 1.0
 */
public class Flink03_State_Map_State_Count {

    public static void main(String[] args) throws Exception {


        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.addSource(new ClickSource())
                .keyBy(r -> r.getName())
                .process(new ClickProcess())
                .print();

        env.execute();


    }

    private static class ClickProcess extends KeyedProcessFunction<String, ClickSource.Click, String> {
        private MapState<String, Integer> mapState;

        @Override
        public void open(Configuration parameters) throws Exception {
            mapState = getRuntimeContext().getMapState(
                    new MapStateDescriptor<String, Integer>(
                            "mapState",
                            Types.STRING,
                            Types.INT));

        }

        @Override
        public void processElement(ClickSource.Click in, Context ctx, Collector<String> out) throws Exception {

            //判断来的数据是否在状态中保存了.
            if (!mapState.contains(in.getUrl())) {
                //如果没保存就是第一次访问
                mapState.put(in.getUrl(), 1);
            } else {
                //访问量加1
                Integer oldCount = mapState.get(in.getUrl());
                int newCount = oldCount + 1;
                mapState.put(in.getUrl(), newCount);
            }

            StringBuilder sb = new StringBuilder();
            sb.append(ctx.getCurrentKey() + "{\n");
            for (String key : mapState.keys()) {
                sb.append(" ")
                        .append("\"" + key + "\" ->")
                        .append(mapState.get(key)).append(",\n");
            }
            sb.append("}\n");
            out.collect(sb.toString());

        }
    }
}
