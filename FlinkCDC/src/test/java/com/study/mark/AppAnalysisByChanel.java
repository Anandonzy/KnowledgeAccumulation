package com.study.mark;

import com.flink.appAnalysis.Flink04ProjectAppAnalysisByChanel;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @Author wangziyu1
 * @Date 2023/2/6 16:58
 * @Version 1.0
 */
public class AppAnalysisByChanel {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        env.addSource(new Flink04ProjectAppAnalysisByChanel.AppMarketingDataSource())
                .map(behavior -> Tuple2.of(behavior.getBehavior(), 1L))
                .returns(Types.TUPLE(Types.STRING, Types.LONG))
                .keyBy(f -> f.f0)
                .sum(1)
                .print();

        env.execute();


    }
}
