package com.study.knowlages.juc.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Author wangziyu1
 * @Date 2022/2/23 3:12 下午
 * @Version 1.0
 * 手写一个自旋锁
 * 自旋锁：spinlock，是指尝试获取锁的线程不会立即阻塞，而是采用循环的方式去尝试获取锁，这样的好处是减少线程上下文切换的消耗，缺点是循环会消耗CPU
 * <p>
 * 通过CAS操作完成自旋锁，A线程先进来调用myLock方法自己持有锁5秒，B随后进来发现当前有线程持有锁，不是null，所以只能通过自旋等待，直到A释放锁后B随后抢到
 */

public class SpinLockDemo {


    //原子引用  现在的泛型装的是Thread，原子引用线程
    AtomicReference atomicReference = new AtomicReference<Thread>();

    public void mylock() {
        //获取当前的线程
        Thread thread = Thread.currentThread();
        System.out.println(Thread.currentThread().getName() + "\t come in ");

        //开始自选 期望是null ,更新当前的线程 如果是null ,则更新为当前的线程,否则自选
        while (!atomicReference.compareAndSet(null, thread)) {

        }
    }

    /**
     * 解锁
     */
    public void myUnlock() {
        //获取当前的线程
        Thread thread = Thread.currentThread();

        //自己用完了给线程改为null
        atomicReference.compareAndSet(thread, null);
        System.out.println(Thread.currentThread().getName() + "\t invoked myUnlock()");
    }

    public static void main(String[] args) {

        SpinLockDemo spinLockDemo = new SpinLockDemo();
        //启动t1线程
        new Thread(() -> {
            spinLockDemo.mylock();

            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 开始释放锁
            spinLockDemo.myUnlock();
        }, "t1").start();

        // 让main线程暂停1秒，使得t1线程，先执行
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 1秒后，启动t2线程，开始占用这个锁
        new Thread(() -> {

            // 开始占有锁
            spinLockDemo.mylock();
            // 开始释放锁
            spinLockDemo.myUnlock();

        }, "t2").start();
        /**
         * t1 come in
         * .....一秒后.....
         * t2 come in
         * .....五秒后.....
         * t1 invoked myUnlock()
         * t2 invoked myUnlock()
         *
         * 首先输出的是 t1 come in
         *
         * 然后1秒后，t2线程启动，发现锁被t1占有，所有不断的执行 compareAndSet方法，来进行比较，直到t1释放锁后，也就是5秒后，t2成功获取到锁，然后释放
         */

    }
}
