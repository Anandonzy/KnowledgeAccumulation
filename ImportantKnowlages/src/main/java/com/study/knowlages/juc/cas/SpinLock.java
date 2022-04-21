package com.study.knowlages.juc.cas;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Author wangziyu1
 * @Date 2022/4/1 17:20
 * @Version 1.0
 * 手写自旋锁
 */
public class SpinLock {
    public AtomicReference<Thread> atomicReference = new AtomicReference<Thread>();

    public void mylock(){
        Thread thread = Thread.currentThread();
        System.out.println(Thread.currentThread().getName() + "\t ---come in");
        while (!atomicReference.compareAndSet(null, thread)) {

        }
        System.out.println(Thread.currentThread().getName() + "\t 抢锁成功");
    }

    public void myUnlock(){
        atomicReference.compareAndSet(Thread.currentThread(), null);
        System.out.println(Thread.currentThread().getName() + "\t 释放锁成功~");
    }

    public static void main(String[] args) {

        SpinLock spinLock = new SpinLock();
        new Thread(() -> {
            spinLock.mylock();
            try {
                TimeUnit.SECONDS.sleep(3L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            spinLock.myUnlock();
        }, "t1").start();

        new Thread(() -> {
            spinLock.mylock();
            spinLock.myUnlock();
        }, "t2").start();
    }
}
