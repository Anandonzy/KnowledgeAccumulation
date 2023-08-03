package com.flink.api.soruce;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.Random;

/**
 * @Author wangziyu1
 * @Date 2022/11/8 09:41
 * @Version 1.0
 */
public class SensorSource implements SourceFunction<Sensor> {


    private Random random = new Random();
    private Boolean isRunning = true;

    @Override
    public void run(SourceContext ctx) throws Exception {

        while (isRunning) {
            for (int i = 1; i < 4; i++) {
                ctx.collect(new Sensor("Sensor_" + i, random.nextGaussian()));
                Thread.sleep(300L);
            }
        }
    }

    @Override
    public void cancel() {
        isRunning = false;
    }
}
