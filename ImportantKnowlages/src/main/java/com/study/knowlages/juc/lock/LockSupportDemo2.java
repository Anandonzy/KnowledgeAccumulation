package com.study.knowlages.juc.lock;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author wangziyu1
 * @Date 2022/3/16 5:04 下午
 * @Version 1.0
 * <p>
 * Condition接口中的await后signal方法实现线程的等待和唤醒，与Object类中的wait和notify方法实现线程等待和唤醒类似。
 *
 * await和signal方法必须要在同步块或者方法里面且成对出现使用，否则会抛出java.lang.IllegalMonitorStateException。
 * 调用顺序要先await后signal才OK。
 */
public class LockSupportDemo2 {

    public static Lock lock = new ReentrantLock();
    public static Condition condition = lock.newCondition();

    public static void main(String[] args) {

        new Thread(() -> {
            try {
                //try { TimeUnit.SECONDS.sleep(3L);} catch (InterruptedException e) {e.printStackTrace();}
                lock.lock();
                System.out.println(Thread.currentThread().getName() + "\t is come in");
                condition.await();
                System.out.println(Thread.currentThread().getName() + "\t 被唤醒");

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }

        }, "A").start();

        new Thread(() -> {
            try {
                lock.lock();
                condition.signal();
                System.out.println(Thread.currentThread().getName() + "\t 通知");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }, "B").start();
    }
}
