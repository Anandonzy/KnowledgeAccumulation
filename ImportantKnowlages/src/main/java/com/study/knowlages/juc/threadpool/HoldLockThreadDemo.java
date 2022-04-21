package com.study.knowlages.juc.threadpool;

import java.util.concurrent.TimeUnit;

/**
 * @Author wangziyu1
 * @Date 2022/3/4 4:34 下午
 * @Version 1.0
 * 死锁代码演示
 * 我们创建了一个资源类，然后让两个线程分别持有自己的锁，同时在尝试获取别人的，就会出现死锁现象
 */

class HoldLockThread implements Runnable {

    private String lockA;
    private String lockB;

    public HoldLockThread(String lockA, String lockB) {
        this.lockA = lockA;
        this.lockB = lockB;
    }

    @Override
    public void run() {

        //自己持有的A锁尝试获得B锁
        synchronized (lockA){
            System.out.println(Thread.currentThread().getName() + "\t 自己持有" + lockA + "\t 尝试获取：" + lockB);

            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            synchronized (lockB) {
                System.out.println(Thread.currentThread().getName() + "\t 自己持有" + lockB + "\t 尝试获取：" + lockA);
            }
        }

    }
}

/**
 * 程序运行的之后使用命令查看  进程
 * jps -l 找到进程号 jps输出的该类的pid 88369
 * 然后
 * jstack 88369 查看日志
 */
public class HoldLockThreadDemo {

    public static void main(String[] args) {
        String lockA = "lockA";
        String lockB = "lockB";

        new Thread(new HoldLockThread(lockA, lockB), "t1").start();

        new Thread(new HoldLockThread(lockB, lockA), "t2").start();
    }
}
