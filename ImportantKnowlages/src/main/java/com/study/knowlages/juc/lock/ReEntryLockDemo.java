package com.study.knowlages.juc.lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author wangziyu1
 * @Date 2022/3/30 17:30
 * @Version 1.0
 * 指的是可重复可递归调用的锁，在外层使用锁之后，在内层仍然可以使用并且不发生死锁，这样的锁就叫做可重入锁。
 * 简单的来说就是：
 *
 * 在一个synchronized修饰的方法或代码块的内部调用本类的其他synchronized修饰的方法或代码块时，是永远可以得到锁的
 */
public class ReEntryLockDemo {
    static Object objectLock = new Object();
    public static void syncBlock()
    {
        new Thread(() -> {
            synchronized (objectLock) {// lock
                System.out.println("-----外层");
                synchronized (objectLock)
                {
                    System.out.println("-----中层");
                    synchronized (objectLock)
                    {
                        System.out.println("-----内层");
                    }
                }
            }//unlock
        },"t1").start();
    }
    public synchronized void m1()
    {
        m1();
    }

    public static void main(String[] args)
    {
        Lock lock = new ReentrantLock();

        new Thread(() -> {
            lock.lock();
            try
            {
                System.out.println(Thread.currentThread().getName()+"\t"+"-----外层");
                lock.lock();
                try
                {
                    System.out.println(Thread.currentThread().getName()+"\t"+"-----内层");
                }finally {
                    lock.unlock();
                }
            }finally {
                lock.unlock();
            }
        },"t1").start();

        new Thread(() -> {
            lock.lock();
            try
            {
                System.out.println("------22222");
            }finally {
                lock.unlock();
            }
        },"t2").start();
    }



}
