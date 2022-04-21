package com.study.knowlages.juc.lock;

/**
 * @Author wangziyu1
 * @Date 2022/3/16 4:56 下午
 * @Version 1.0
 * wait/Notify 的Demo 为了对比LockSupport
 *
 * wait和notify方法必须要在同步块或者方法里面且成对出现使用， 说白了必须要有synchronized锁
 * 否则会抛出java.lang.IllegalMonitorStateException
 * 调用顺序要先wait后notify才OK。
 */
public class LockSupportDemo {
    static Object lock = new Object();
    public static void main(String[] args) {
        new Thread(()->{
            synchronized (lock) {
                System.out.println(Thread.currentThread().getName()+" come in.");
                try {
                    lock.wait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.out.println(Thread.currentThread().getName()+" 换醒.");
        }, "Thread A").start();

        new Thread(()->{
            synchronized (lock) {
                lock.notify();
                System.out.println(Thread.currentThread().getName()+" 通知.");
            }
        }, "Thread B").start();
    }
}
