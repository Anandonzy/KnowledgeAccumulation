package com.study.knowlages.juc.threadpool;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author wangziyu1
 * @Date 2022/3/3 11:08 上午
 * @Version 1.0 x
 * 创建线程池:
 * Executors.newFixedThreadPool(int i) :创建一个拥有i个线程的线程池
 * 执行长期的任务,性能好很多
 * 创建一个定长线程池,可以控制线程数 最大并发数,超出的线程会在队列中等待.
 */
public class MyThreadPoolDemo {

    public static void main(String[] args) {

        System.out.println(Runtime.getRuntime().availableProcessors());
        //ThreadpPollExecutor
        // Array  Arrays(辅助工具类)
        // Collection Collections(辅助工具类)
        // Executor Executors(辅助工具类)

        /**
         * newFixedThreadPool
         */

//        ExecutorService threadPool = Executors.newFixedThreadPool(1);  //5个固定的线程
//        ExecutorService threadPool = Executors.newSingleThreadExecutor();//一个池一个线程
        ExecutorService threadPool = Executors.newCachedThreadPool();

        //模拟10个人 来办理业务,每个用户就是一个来自外部的请求.
        try {

            for (int i = 0; i < 10; i++) {
                int j = i;
                threadPool.execute(() -> {
                    System.out.println(Thread.currentThread().getName() + "\t 开始办理" + j + "业务");
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }

    }
}
