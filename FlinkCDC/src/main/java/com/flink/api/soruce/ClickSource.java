package com.flink.api.soruce;

import lombok.*;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.Calendar;
import java.util.Random;

/**
 * @Author wangziyu1
 * @Date 2022/11/7 17:02
 * @Version 1.0
 */
public class ClickSource implements SourceFunction<ClickSource.Click> {
    private Boolean flag = true;


    @Override
    public void run(SourceContext<Click> sourceContext) throws Exception {
        String[] names = {"Bob", "Tom", "Json"};
        String[] urls = {"/home", "/cart"};
        Random random = new Random();

        while (flag) {
            sourceContext.collect(new Click(names[random.nextInt(names.length)],urls[random.nextInt(urls.length)],
                    // 获取当前的机器时间，作为事件的事件时间
                    Calendar.getInstance().getTimeInMillis()));
            Thread.sleep(1000L);
        }

    }

    @Override
    public void cancel() {


    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Click {
        private String name;
        private String url;
        private Long ts;
    }

    public static void main(String[] args) throws InterruptedException {
        String[] names = {"Bob", "Tom", "Json"};
        String[] urls = {"/home", "/cart"};
        Random random = new Random();

        while (true) {
            System.out.println((new Click(names[random.nextInt(names.length)], urls[random.nextInt(urls.length)],
                    // 获取当前的机器时间，作为事件的事件时间
                    Calendar.getInstance().getTimeInMillis())));
            Thread.sleep(1000L);
        }
    }
}
