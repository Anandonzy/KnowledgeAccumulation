package com.flink.ads;

import com.flink.bean.AdsClickLog;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import static org.apache.flink.api.common.typeinfo.Types.*;

/**
 * @version 1.0
 * @Author wangziyu1
 * @Date 9/18/21 4:59 PM
 * @注释
 */
public class Flink06_Project_Ads_Click {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env
                .readTextFile("/Users/wangziyu/Desktop/data/AdClickLog.csv")
                .map(line -> {
                    String[] split = line.split(",");
                    return new AdsClickLog(Long.valueOf(split[0]),
                            Long.valueOf(split[1]),
                            split[2],
                            split[3],
                            Long.valueOf(split[4])
                    );
                })
                .map(log -> Tuple2.of(Tuple2.of(log.getProvince(), log.getAdId()), 1L))
                .returns(Types.TUPLE(Types.TUPLE(STRING,LONG), LONG))
                .keyBy(new KeySelector<Tuple2<Tuple2<String, Long>, Long>, Tuple2<String, Long>>() {
                    @Override
                    public Tuple2<String, Long> getKey(Tuple2<Tuple2<String, Long>, Long> value) throws Exception {

                        return value.f0;
                    }
                }).sum(1)
                .print("省份广告");
        env.execute();


    }
    }
