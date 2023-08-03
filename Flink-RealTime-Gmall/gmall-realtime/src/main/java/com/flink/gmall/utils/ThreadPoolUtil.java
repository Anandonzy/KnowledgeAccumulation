package com.flink.gmall.utils;


import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author wangziyu1
 * @Date 2022/9/2 17:09
 * @Version 1.0
 * 获取线程池的工具类
 * 单例设计模式 懒汉
 * DCL双端检索
 */
public class ThreadPoolUtil {

    private static ThreadPoolExecutor threadPoolExecutor;


    public static ThreadPoolExecutor getThreadPoolExecutor() {

        //单例设计模式 懒汉试 DCL双端监测
        if (threadPoolExecutor == null) {
            synchronized (ThreadPoolUtil.class) {
                if (threadPoolExecutor == null) {
                    threadPoolExecutor = new ThreadPoolExecutor(4, 20, 100, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
                }
            }
        }
        return threadPoolExecutor;
    }
}
