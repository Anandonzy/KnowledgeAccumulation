package com.flink.trigger;

import com.flink.bean.WaterSensor;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.time.Duration;

/**
 * @version 1.0
 * @Author wangziyu1
 * @Date 9/26/21 4:21 PM
 * @注释
 */
public class Flink13_Trigger_Water_Test {


    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment().setParallelism(1);


        SingleOutputStreamOperator<WaterSensor> stream = env
                .socketTextStream("localhost", 9999)  // 在socket终端只输入毫秒级别的时间戳
                .map(value -> {
                    String[] datas = value.split(",");
                    return new WaterSensor(datas[0], Long.valueOf(datas[1]), Integer.valueOf(datas[2]));
                });


        WatermarkStrategy<WaterSensor> wms = WatermarkStrategy
                .<WaterSensor>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                .withTimestampAssigner((element, recordTimestamp) -> element.getTs() * 1000);
        stream
                .assignTimestampsAndWatermarks(wms)
                .keyBy(WaterSensor::getId)
                .process(new KeyedProcessFunction<String, WaterSensor, String>() {
                    int lastVc = 0;
                    long timerTS = Long.MIN_VALUE;

                    @Override
                    public void processElement(WaterSensor value, Context ctx, Collector<String> out) throws Exception {
                        if (value.getVc() > lastVc) {
                            if (timerTS == Long.MIN_VALUE) {
                                System.out.println("注册....");
                                timerTS = ctx.timestamp() + 5000L;
                                ctx.timerService().registerEventTimeTimer(timerTS);
                            }
                        } else {
                            ctx.timerService().deleteEventTimeTimer(timerTS);
                            timerTS = Long.MIN_VALUE;
                        }

                        lastVc = value.getVc();
                        System.out.println(lastVc);

                    }

                    @Override
                    public void onTimer(long timestamp, OnTimerContext ctx, Collector<String> out) throws Exception {
                        out.collect(ctx.getCurrentKey() + " 报警!!!!");
                        timerTS = Long.MIN_VALUE;
                    }

                }).print();

        env.execute();

    }

}
