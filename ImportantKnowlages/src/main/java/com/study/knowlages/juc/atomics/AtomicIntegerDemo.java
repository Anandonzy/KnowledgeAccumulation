package com.study.knowlages.juc.atomics;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author wangziyu1
 * @Date 2022/4/7 14:29
 * @Version 1.0
 * AtomicInteger演示
 * 常用的Api等.
 * CountDownLatch 配合使用可以最高效的控制线程结束的时间.
 */
class MyNumber {
    public AtomicInteger atomicInteger = new AtomicInteger();

    public void addPlusPlus() {
        atomicInteger.incrementAndGet(); //+1操作
    }
}

public class AtomicIntegerDemo {

    public static final int SIZE_ = 50;

    public static void main(String[] args) throws InterruptedException {
        MyNumber myNumber = new MyNumber();
        //控制线程结束
        CountDownLatch countDownLatch = new CountDownLatch(SIZE_);

        for (int i = 1; i <= SIZE_; i++) { //模拟50个线程 进行点击
            new Thread(() -> {
                try {
                    for (int j = 1; j <= 1000; j++) {
                        myNumber.addPlusPlus();
                    }
                } finally {
                    countDownLatch.countDown();
                }

            }, String.valueOf(i)).start();
        }
        countDownLatch.await();
        System.out.println(Thread.currentThread().getName() + "\t --result is:" + myNumber.atomicInteger.get());
    }
}
