package com.flink.watermark;

import com.flink.bean.WaterSensor;
import org.apache.flink.api.common.eventtime.*;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

/**
 * @version 1.0
 * @Author wangziyu1
 * @Date 9/26/21 10:44 AM
 * @注释
 */
public class Flink11_Chapter07_Period {


    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment().setParallelism(1);

        SingleOutputStreamOperator<WaterSensor> stream = env
                .socketTextStream("localhost", 9999)  // 在socket终端只输入毫秒级别的时间戳
                .map(new MapFunction<String, WaterSensor>() {
                    @Override
                    public WaterSensor map(String value) throws Exception {
                        String[] datas = value.split(",");
                        return new WaterSensor(datas[0], Long.valueOf(datas[1]), Integer.valueOf(datas[2]));

                    }
                });


        //创建水印的策略
        WatermarkStrategy<WaterSensor> mywms = new WatermarkStrategy<WaterSensor>() {
            @Override
            public WatermarkGenerator<WaterSensor> createWatermarkGenerator(WatermarkGeneratorSupplier.Context context) {
                System.out.println("createWatermarkGenerator ....");
                return new MyPeriod(3);
            }
        }.withTimestampAssigner(new SerializableTimestampAssigner<WaterSensor>() {
            @Override
            public long extractTimestamp(WaterSensor element, long recordTimestamp) {
                System.out.println("recordTimestamp  " + recordTimestamp);
                return element.getTs() * 1000;
            }

        });

        stream.assignTimestampsAndWatermarks(mywms)
                .keyBy(WaterSensor::getId)
                .window(SlidingEventTimeWindows.of(Time.seconds(5), Time.seconds(5)))
                .process(new ProcessWindowFunction<WaterSensor, String, String, TimeWindow>() {
                    @Override
                    public void process(String key, Context context, Iterable<WaterSensor> elements, Collector<String> out) throws Exception {

                        String msg = "当前key: " + key
                                + "窗口: [" + context.window().getStart() / 1000 + "," + context.window().getEnd() / 1000 + ") 一共有 "
                                + elements.spliterator().estimateSize() + "条数据 ";
                        out.collect(context.window().toString());
                        out.collect(msg);

                    }
                }).print();
        env.execute();


    }

    public static class MyPeriod implements WatermarkGenerator<WaterSensor> {
        private long maxTs = Long.MIN_VALUE;
        // 允许的最大延迟时间 ms
        private final long maxDelay;

        public MyPeriod(long maxDelay) {
            this.maxDelay = maxDelay * 1000;
            this.maxTs = Long.MIN_VALUE + this.maxDelay + 1;
        }


        @Override
        public void onEvent(WaterSensor waterSensor, long eventTimestamp, WatermarkOutput watermarkOutput) {

            System.out.println("onEvent..." + eventTimestamp);
            //有了新的元素找到最大的时间戳
            maxTs = Math.max(maxTs, eventTimestamp);
            System.out.println(maxTs);
        }

        @Override
        public void onPeriodicEmit(WatermarkOutput output) {

//            System.out.println("onPeriodicEmit...");
            // 周期性的发射水印: 相当于Flink把自己的时钟调慢了一个最大延迟
            output.emitWatermark(new Watermark(maxTs - maxDelay - 1));
        }
    }
}
