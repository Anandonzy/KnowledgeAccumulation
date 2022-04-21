package com.study.knowlages.juc.thread;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @Author wangziyu1
 * @Date 2022/2/25 3:30 下午
 * @Version 1.0
 * 信号量
 */
public class SemaphoreDemo {

    public static void main(String[] args) {


        /**
         * 初始化一个信号量为3，默认是false 非公平锁， 模拟3个停车位
         */

        Semaphore semaphore = new Semaphore(3, false);

        for (int i = 1; i <= 6; i++) {
            new Thread(()->{

                try {
                    semaphore.acquire();
                    System.out.println(Thread.currentThread().getName()+"\t抢到车位");

                    //停车三秒钟
                    TimeUnit.SECONDS.sleep(3);
                    System.out.println(Thread.currentThread().getName()+"\t停车三秒后,离开车位");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    semaphore.release();
                }
            },String.valueOf(i)).start();
        }


    }
}
