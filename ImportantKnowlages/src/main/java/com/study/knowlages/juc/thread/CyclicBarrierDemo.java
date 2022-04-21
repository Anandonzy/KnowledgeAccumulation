package com.study.knowlages.juc.thread;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * @Author wangziyu1
 * @Date 2022/2/24 5:46 下午
 * @Version 1.0
 * CountDownLatch 的相反的部分 CyclicBarrier
 * CountDownLatch是作减法 CyclicBarrier是作加法
 * 集齐七颗龙珠 就可以召唤神龙
 */
public class CyclicBarrierDemo {


    public static void main(String[] args) {


        /**
         * 定义一个循环屏障，参数1：需要累加的值，参数2 需要执行的方法
         */
        CyclicBarrier cyclicBarrier = new CyclicBarrier(7, () -> {
            System.out.println("召唤神龙");
        });

        //开始收集龙珠
        for (int i = 1; i <= 7; i++) {

            final int tempInt = i;

            new Thread(() -> {
                System.out.println(Thread.currentThread().getName() + "\t 收集到第"+tempInt+"龙珠 ");

                try {
                    cyclicBarrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }, String.valueOf(i)).start();

        }

    }
}
