package com.study.knowlages.juc.threadpool;

import java.util.concurrent.*;

/**
 * @Author wangziyu1
 * @Date 2022/3/3 5:11 下午
 * @Version 1.0 自定义线程池
 * 手写线程池
 */
public class MyThreadPoolDemo2 {


    public static void main(String[] args) {


        ExecutorService threadPoolExecutor = new ThreadPoolExecutor(2, //核心容量
                5,//最大容量
                1L,//达到最大容量之后销毁的等待时间
                TimeUnit.SECONDS,//销毁时间的单位
                new LinkedBlockingDeque<>(3),//阻塞队列最大长度 不给的话,默认21亿一定要给
                Executors.defaultThreadFactory(),//使用默认的线程创建工厂即可
                new ThreadPoolExecutor.AbortPolicy());//拒绝策略 使用RejectedExcutionException异常，阻止系统正常运行

        try {
            for (int i = 1; i <= 13; i++) {
                int j = i;
                threadPoolExecutor.execute(()->{
                    System.out.println(Thread.currentThread().getName()+"\t 开始处理:"+j);
                });

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadPoolExecutor.shutdown();
        }


    }
}
