package com.study.knowlages.juc.atomics;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * @Author wangziyu1
 * @Date 2022/4/7 16:30
 * @Version 1.0
 * AtomicReferenceFieldUpdater 演示
 * 多个资源抢初始化
 * 多线程并发调一个类的初始化方法,如果未被初始化过了,将执行初始化工作,要求只能初始化一次.
 */

class MyVar {
    public volatile Boolean isInit = Boolean.FALSE;
    AtomicReferenceFieldUpdater<MyVar, Boolean> fieldUpdater = AtomicReferenceFieldUpdater.newUpdater(MyVar.class, Boolean.class, "isInit");

    /**
     * 以一种线程安全的方式 初始化
     */
    public void init(MyVar myVar) {
        if (fieldUpdater.compareAndSet(myVar, Boolean.FALSE, Boolean.TRUE)) {
            System.out.println(Thread.currentThread().getName() + "\t ---start init");
            try {
                TimeUnit.SECONDS.sleep(3L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + "\t ---end init");
        } else {
            System.out.println(Thread.currentThread().getName() + "\t" + "---抢夺失败，已经有线程在修改中");
        }
    }
}

public class AtomicReferenceFieldUpdaterDemo {
    public static void main(String[] args) {
        MyVar myVar = new MyVar();

        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                myVar.init(myVar);
            }, String.valueOf(i)).start();
        }
    }
}
