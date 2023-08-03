package com.flink.connect;

import com.flink.api.soruce.ClickSource;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoFlatMapFunction;
import org.apache.flink.util.Collector;

/**
 * @Author wangziyu1
 * @Date 2022/11/14 16:58
 * @Version 1.0
 * 查询流
 * 测试connect和broadcast的使用
 * connect
 * <p>
 * 只能合并两条流
 * <p>
 * 两条流的元素的类型可以不一样
 * <p>
 * DataStream API
 * <p>
 * CoMapFunction<IN1, IN2, OUT>
 * <p>
 * map1
 * map2
 * CoFlatMapFunction<IN1, IN2, OUT>
 * <p>
 * flatMap1：来自第一条流的事件进入CoFlatMapFunction，触发调用。
 * flatMap2：来自第二条流的事件进入CoFlatMapFunction，触发调用。
 * 底层API
 * <p>
 * CoProcessFunction<IN1, IN2, OUT>
 * <p>
 * processElement1
 * processElement2
 * onTimer
 * 状态变量
 * 应用场景
 * <p>
 * 一条流进行keyBy，另一条流broadcast，使的所有的逻辑分区都能和同一条流进行JOIN。
 * 两条流都进行keyBy，将来自两条流的相同key的数据合并在一起处理，也就是说，将来自两条流的相同key的数据放在一个逻辑分区中做处理。
 */
public class ConnectBroadcastDemo {


    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);

        DataStreamSource<ClickSource.Click> clickSource = env.addSource(new ClickSource());

        DataStreamSource<String> querySource = env.socketTextStream("localhost", 9999);


        //这个地方
        //数据流在广播之前一定要设置为1
        //为了使下游的所有的并行子任务看到相同的数据
        clickSource.keyBy(ClickSource.Click::getUrl)
                .connect(querySource.setParallelism(1).broadcast())
                .flatMap(new CoFlatMapFunction<ClickSource.Click, String, ClickSource.Click>() {

                    private String queryStr = "";

                    @Override
                    public void flatMap1(ClickSource.Click in1, Collector<ClickSource.Click> out) throws Exception {
                        if (in1.getName().equals(queryStr)) {
                            out.collect(in1);
                        }
                    }

                    @Override
                    public void flatMap2(String in2, Collector<ClickSource.Click> out) throws Exception {
                        queryStr = in2;
                    }
                })
                .print();


        env.execute();


    }
}
