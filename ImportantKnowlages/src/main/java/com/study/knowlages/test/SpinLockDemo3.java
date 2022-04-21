package com.study.knowlages.test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Author wangziyu1
 * @Date 2022/4/1 17:27
 * @Version 1.0 手写自旋锁练习
 */
public class SpinLockDemo3 {
    public AtomicReference atomicReference = new AtomicReference<Thread>();

    public void mylock() {
        Thread thread = Thread.currentThread();
        System.out.println(Thread.currentThread().getName() + "\t ---is come in");
        while (!atomicReference.compareAndSet(null, thread)) {

        }
        System.out.println(Thread.currentThread().getName() + "\t --- 抢占锁成功");
    }

    public void myunlock() {
        atomicReference.compareAndSet(Thread.currentThread(), null);
        System.out.println(Thread.currentThread().getName() + "\t --- 释放锁成功~");
    }

    public static void main(String[] args) {

        SpinLockDemo3 spinLockDemo3 = new SpinLockDemo3();
        new Thread(() -> {
            spinLockDemo3.mylock();
            try {
                TimeUnit.SECONDS.sleep(3L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            spinLockDemo3.myunlock();
        }, "t1").start();

        new Thread(() -> {
            spinLockDemo3.mylock();
            spinLockDemo3.myunlock();
        }, "t2").start();

        List<Integer> list = Arrays.asList(1, 2, 3, 4);
        list.forEach(System.out::println);
        System.out.println();


    }

}
