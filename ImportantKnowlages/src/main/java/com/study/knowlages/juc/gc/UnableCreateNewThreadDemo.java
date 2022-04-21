package com.study.knowlages.juc.gc;

import java.util.concurrent.TimeUnit;

/**
 * @Author wangziyu1
 * @Date 2022/3/8 11:36 上午
 * @Version 1.0
 */
public class UnableCreateNewThreadDemo {

    public static void main(String[] args) {
        for (int i = 0; ; i++) {
            System.out.println("************** i = " + i);
            new Thread(() -> {
                try {
                    TimeUnit.SECONDS.sleep(Integer.MAX_VALUE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, String.valueOf(i)).start();
        }
    }
}
