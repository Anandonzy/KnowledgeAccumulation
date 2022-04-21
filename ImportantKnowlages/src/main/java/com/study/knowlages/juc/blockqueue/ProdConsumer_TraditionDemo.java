package com.study.knowlages.juc.blockqueue;

/**
 * @Author wangziyu1
 * @Date 2022/3/1 4:11 下午
 * @Version 1.0
 * <p>
 * 传统=生产消费者
 * 一个初始值为0的变量，两个线程对其交替操作，一个加1，一个减1，来5轮
 * 线程 操作 资源类
 * 判断 干活 通知
 * 防止虚假唤醒机制
 */

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 资源类
 */
class ShareData {
    private int number = 0;
    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();


    /**
     * 加1
     *
     * @throws Exception
     */
    public void increment() throws Exception {
        try {
            lock.lock();
            //1.判断
            while (number != 0) {
                //等待 不能生产
                condition.await();
            }
            //2.干活
            number++;
            System.out.println(Thread.currentThread().getName() + "\t" + number);
            //3.通知唤醒
            condition.signalAll();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 减的操作
     *
     * @throws Exception
     */
    public void decrement() throws Exception {

        try {
            lock.lock();
            //1.判断
            while (number == 0) {
                condition.await();
            }
            //2.干活
            number--;
            System.out.println(Thread.currentThread().getName() + "\t" + number);

            //3.通知唤醒
            condition.signalAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}

public class ProdConsumer_TraditionDemo {

    public static void main(String[] args) {

        ShareData shareData = new ShareData();
        new Thread(() -> {
            for (int i = 1; i <= 5 ;i++){
                try {
                    shareData.increment();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "A").start();

        new Thread(() -> {

            for (int i = 1; i <= 5 ;i++){
                try {
                    shareData.decrement();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "B").start();

    }
}
