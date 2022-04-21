package com.study.knowlages.test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Author wangziyu1
 * @Date 2022/2/23 4:02 下午
 * @Version 1.0
 * 手写自旋锁练习
 * 没有wait的阻塞
 * 占用cpu
 */
public class SpinLockDemo2 {

    //原子引用
    AtomicReference atomicReference = new AtomicReference<Thread>();

    public void myLock() {
        Thread thread = Thread.currentThread();
        System.out.println(Thread.currentThread().getName() + "\t myLock in come");
        while (!atomicReference.compareAndSet(null, thread)) {

        }
    }

    public void myUnLock() {

        Thread thread = Thread.currentThread();
        System.out.println(Thread.currentThread().getName()+ "\t myLock in myUnLock");
        atomicReference.compareAndSet(thread, null);
    }

    public static void main(String[] args) {

        SpinLockDemo2 spinLockDemo2 = new SpinLockDemo2();
        //启动t1
        new Thread(()->{
            spinLockDemo2.myLock();
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            spinLockDemo2.myUnLock();
        },"t1").start();

        //保证t1 限制性
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new Thread(()->{
            spinLockDemo2.myLock();
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            spinLockDemo2.myUnLock();
        },"t2").start();

    }
}
