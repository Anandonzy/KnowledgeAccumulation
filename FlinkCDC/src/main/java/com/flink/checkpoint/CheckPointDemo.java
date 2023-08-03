package com.flink.checkpoint;

import com.flink.api.soruce.ClickSource;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @Author wangziyu1
 * @Date 2022/11/18 17:41
 * @Version 1.0
 * 测试本地checkpoint
 */
public class CheckPointDemo {


    public static void main(String[] args) throws Exception {


        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);

        env.enableCheckpointing(10 * 1000L);

        env.setStateBackend(new FsStateBackend("file:///Users/wangziyu/Downloads/checkpoint_test"));

        env.addSource(new ClickSource())
                .print();


        env.execute();


    }
}
