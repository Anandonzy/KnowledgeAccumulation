package com.study.knowlages.juc.blockqueue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author wangziyu1
 * @Date 2022/3/2 3:04 下午
 * @Version 3.0
 * 生产消费者Demo升级版 包含juc全部的知识
 * 在concurrent包发布以前，在多线程环境下，我们每个程序员都必须自己去控制这些细节，尤其还要兼顾效率和线程安全，则这会给我们的程序带来不小的时间复杂度
 * <p>
 * 现在我们使用新版的阻塞队列版生产者和消费者，使用：volatile、CAS、atomicInteger、BlockQueue、线程交互、原子引用
 */

class MyResource {

    //默认开启 进行生产消费
    private volatile boolean FLAG = true;

    //类似 number++ 我们需要原子类 保证原子性
    private AtomicInteger atomicInteger = new AtomicInteger(0);

    BlockingQueue<String> blockingQueue = null;

    public MyResource(BlockingQueue<String> blockingQueue) {
        this.blockingQueue = blockingQueue;
        System.out.println(blockingQueue.getClass().getName()); //可以在这里打日志 看到传来的是什么
    }


    /**
     * 生产
     *
     * @throws Exception
     */
    public void myProd() throws Exception {
        String data = null;
        boolean retValue;
        // 多线程环境的判断，一定要使用while进行，防止出现虚假唤醒
        // 当FLAG为true的时候，开始生产
        while (FLAG) {
            data = atomicInteger.incrementAndGet() + "";

            // 2秒存入1个data
            retValue = blockingQueue.offer(data, 2L, TimeUnit.SECONDS);
            if (retValue) {
                System.out.println(Thread.currentThread().getName() + "\t 插入队列:" + data + "成功");
            } else {
                System.out.println(Thread.currentThread().getName() + "\t 插入队列:" + data + "失败");
            }

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println(Thread.currentThread().getName() + "\t 停止生产，表示FLAG=false，生产介绍");
    }

    /**
     * 消费
     *
     * @throws Exception
     */
    public void myConsumer() throws Exception {
        String retValue;
        // 多线程环境的判断，一定要使用while进行，防止出现虚假唤醒
        // 当FLAG为true的时候，开始生产
        while (FLAG) {
            // 2秒存入1个data
            retValue = blockingQueue.poll(2L, TimeUnit.SECONDS);
            if (retValue != null && retValue != "") {
                System.out.println(Thread.currentThread().getName() + "\t 消费队列:" + retValue + "成功");
            } else {
                FLAG = false;
                System.out.println(Thread.currentThread().getName() + "\t 消费失败，队列中已为空，退出");

                // 退出消费队列
                return;
            }
        }
    }


    /**
     * 停止生产的判断
     */
    public void stop() {
        this.FLAG = false;
    }
}

public class ProdConsumerBlockingQueueDemo {

    public static void main(String[] args) {
        MyResource myResource = new MyResource(new ArrayBlockingQueue<String>(10));

        new Thread(() -> {
            try {
                System.out.println(Thread.currentThread().getName() + "\t 生产线程启动");
                myResource.myProd();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }, "prod").start();

        new Thread(() -> {
            try {
                System.out.println(Thread.currentThread().getName() + "\t 消费线程启动");
                myResource.myConsumer();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }, "consumer").start();


        // 5秒后，停止生产和消费
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("");
        System.out.println("");
        System.out.println("5秒中后，生产和消费线程停止，线程结束");
        myResource.stop();

    }
}
