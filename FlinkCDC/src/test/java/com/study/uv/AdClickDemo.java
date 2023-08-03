package com.study.uv;

import com.flink.bean.AdsClickLog;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @Author wangziyu1
 * @Date 2023/2/6 17:22
 * @Version 1.0
 * 不同省份不同广告的点击量
 */
public class AdClickDemo {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        env
                .readTextFile("/Users/wangziyu/Desktop/data/AdClickLog.csv")
                .map(new MapFunction<String, Tuple2<String,Long>>() {
                    @Override
                    public Tuple2<String, Long> map(String line) throws Exception {
                        String[] split = line.split(",");
                        AdsClickLog log = new AdsClickLog(Long.valueOf(split[0]),
                                Long.valueOf(split[1]),
                                split[2],
                                split[3],
                                Long.valueOf(split[4]));

                        return Tuple2.of(log.getProvince() + "_" + log.getAdId(), 1L);
                    }
                })
                .keyBy(date -> date.f0)
                .sum(1)
                .print();


        env.execute();


    }
}
