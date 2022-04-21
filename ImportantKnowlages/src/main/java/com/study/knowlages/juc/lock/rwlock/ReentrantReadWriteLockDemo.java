package com.study.knowlages.juc.lock.rwlock;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @Author wangziyu1
 * @Date 2022/4/19 11:01
 * @Version 1.0
 * 通过演示ReentrantLock 和 ReentrantReadWriteLock 的演变过程展示区别
 * ReentrantLock
 *  读读只能一个
 *  写写只能一个
 *  读写只能一个
 * ReentrantReadWriteLock
 *  读读能多个
 *  写写只能一个
 *  读写只能一个
 */

class MyResource {
    Map<String, String> map = new HashMap();

    //=====ReentrantLock 等价于 =====synchronized
    //使用可重入锁 读读不能共存 只能一个写入一个读取
    Lock lock = new ReentrantLock();

    //=====ReentrantReadWriteLock 一体两面，读写互斥，读读共享
    ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock();

    public void write(String key, String value) {
        try {
            //lock.lock();
            rwlock.writeLock().lock();
            System.out.println(Thread.currentThread().getName() + "\t 开始写入");
            map.put(key, value);
            System.out.println(Thread.currentThread().getName() + "\t 写入完成");
        } finally {
            rwlock.writeLock().unlock();
            //lock.unlock();
        }
    }

    public void read(String key) {
        try {
            //lock.lock();
            rwlock.readLock().lock();
            System.out.println(Thread.currentThread().getName() + "\t 开始读取");
            String result = map.get(key);
            System.out.println(Thread.currentThread().getName() + "\t 读取完成 result:" + result);
        } finally {
            rwlock.readLock().unlock();
            //lock.unlock();
        }
    }
}

public class ReentrantReadWriteLockDemo {

    public static void main(String[] args) {

        MyResource myResource = new MyResource();
        for (int i = 1; i <= 10; i++) {
            int finalI = i;
            new Thread(() -> {
                myResource.write(finalI + "", finalI + "");
            }, String.valueOf(i)).start();
        }

        for (int i = 1; i <= 10; i++) {
            int finalI = i;
            new Thread(() -> {
                myResource.read(finalI + "");
            }, String.valueOf(i)).start();
        }


    }


}
