package com.study.knowlages.juc.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * @Author wangziyu1
 * @Date 2022/3/17 10:57 上午
 * @Version 1.0
 * <p>
 * LockSupport 的使用
 */
public class LockSupportDemo3 {

    public static void main(String[] args) {

        Thread a = new Thread(() -> {


            System.out.println(Thread.currentThread().getName() + "\t is come in");
            System.out.println(Thread.currentThread().getName() + "\t"+System.currentTimeMillis());
            LockSupport.park(); //被阻塞 如果想通过需要许可证 许可证最多一个  多个会阻塞
            //LockSupport.park(); //被阻塞 如果想通过需要许可证
            System.out.println(Thread.currentThread().getName() + "\t"+System.currentTimeMillis());
            System.out.println(Thread.currentThread().getName() + "\t is 被唤醒");

        }, "A");
        a.start();

        //暂停3秒
        try {
            TimeUnit.SECONDS.sleep(3L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new Thread(() -> {
            LockSupport.unpark(a);
            //LockSupport.unpark(a); //许可证最多一个  多个会阻塞
            System.out.println(Thread.currentThread().getName() + "\t 通知");
        }, "B").start();
    }
}
