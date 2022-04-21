package com.study.knowlages.juc.atomics;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Author wangziyu1
 * @Date 2022/4/7 15:27
 * @Version 1.0
 * 使用AtomicRef 手写自旋锁
 */
public class SpinLockDemo {
    AtomicReference atomicReference = new AtomicReference<Thread>();

    public void myLock(){
        Thread thread = Thread.currentThread();
        System.out.println(Thread.currentThread().getName() + "\t is come in");

        while (!atomicReference.compareAndSet(null,thread)){

        }
    }
    public void myUnlock(){
        atomicReference.compareAndSet(Thread.currentThread(), null);
        System.out.println(Thread.currentThread().getName() + "\t 锁用完了,释放锁~");
    }


    public static void main(String[] args) {
        SpinLockDemo spinLockDemo = new SpinLockDemo();

        new Thread(() -> {
            spinLockDemo.myLock();

            try {
                TimeUnit.SECONDS.sleep(3L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            spinLockDemo.myUnlock();
        }, "t1").start();
        try {
            TimeUnit.SECONDS.sleep(1L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new Thread(() -> {
            spinLockDemo.myLock();
            spinLockDemo.myUnlock();
        }, "t2").start();


    }
}
