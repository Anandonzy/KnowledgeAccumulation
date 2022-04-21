package com.study.knowlages.juc.thread;

/**
 * @Author wangziyu1
 * @Date 2022/3/2 6:41 下午
 * @Version 1.0
 */

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * 不能返回数据的Runnable接口
 */
class MyThread implements Runnable {
    @Override
    public void run() {

    }
}

/**
 * 能返回数据的Callable接口
 */
class MyThread2 implements Callable {

    @Override
    public Integer call() throws Exception {
        // 5秒后，停止生产和消费
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return 1024;
    }
}


public class CallableDemo {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        FutureTask futureTask = new FutureTask<Integer>(new MyThread2());
//        FutureTask futureTas2 = new FutureTask<>(new MyThread2());
        Thread t1 = new Thread(futureTask,"AA");
//        Thread t2 = new Thread(futureTask,"BB");
        t1.start();

        while(!futureTask.isDone()){

        }
        int result = (int) futureTask.get();
        System.out.println(result);
        System.out.println();
//        t2.start();
        System.out.println(Thread.currentThread().getName()+"*****");

//        System.out.println(futureTas2.get());

    }
}
