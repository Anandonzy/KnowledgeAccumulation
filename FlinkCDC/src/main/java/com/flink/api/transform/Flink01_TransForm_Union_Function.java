package com.flink.api.transform;

import org.apache.flink.streaming.api.datastream.ConnectedStreams;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @version 1.0
 * @Author wangziyu1
 * @Date 9/17/21 3:06 PM
 * @注释
 */
public class Flink01_TransForm_Union_Function {


    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<Integer> stream1 = env.fromElements(1, 2, 3, 4, 5);
        DataStreamSource<Integer> stream2 = env.fromElements(10, 20, 30, 40, 50);
        DataStreamSource<Integer> stream3 = env.fromElements(100, 200, 300, 400, 500);

// 把多个流union在一起成为一个流, 这些流中存储的数据类型必须一样: 水乳交融
        stream1
                .union(stream2)
                .union(stream3)
                .print();
        env.execute();


    }
}
