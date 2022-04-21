package com.study.knowlages.juc.lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author wangziyu1
 * @Date 2022/2/23 2:42 下午
 * @Version 1.0
 */

class Phone2 implements Runnable {

    Lock lock = new ReentrantLock(true);

    @Override
    public void run() {
        getLock();
    }

    /**
     * setLock 进去的时候，就加锁，调用 setLock 方法的时候，能否访问另外一个加锁的 setLock 方法
     */
    private void getLock() {
        try {
            lock.lock();
//            lock.lock();
            System.out.println(Thread.currentThread().getName() + "\t invoked get()");
            setLock();
        } finally {
            lock.unlock();
//            lock.unlock();
        }
    }

    private void setLock() {
        try {
            lock.lock();
//            lock.lock();
            System.out.println(Thread.currentThread().getName() + "\t invoked set()");
        } finally {
            lock.unlock();
//            lock.unlock();
        }
    }
}

public class ReenterLockDemo2 {

    public static void main(String[] args) {


        Phone2 phone = new Phone2();

        // 两个线程操作资源列
        Thread t3 = new Thread(phone,"t3");
        Thread t4 = new Thread(phone,"t4");
        t3.start();
        t4.start();
    }

}
