package com.study.knowlages.juc.myvolatile;

import java.util.concurrent.TimeUnit;

/**
 * Volatile的三大特性:
 * 1.不保证原子性
 * 2.可见性
 * 3.有序性(禁止指令重排)
 */

class MyDate {
    volatile int number = 0;

    public void addTo60() {
        this.number = 60;
    }

}

/**
 * 1.验证Volatile的可见性
 * 1.1 假如 int number=0 ; number变量跟本没加Volatile的关键字修饰
 */
public class VolatileDemo1 {

    public static void main(String[] args) {

        MyDate myDate = new MyDate();

        new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + "\t come in");

            // 线程睡眠3秒，假设在进行运算
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 修改number的值
            myDate.addTo60();

            // 输出修改后的值
            System.out.println(Thread.currentThread().getName() + "\t update number value:" + myDate.number);
        }, "AA").start();

        while (myDate.number == 0) {
            //main 线程一直等待循环,直到number不为0

        }
        // 按道理这个值是不可能打印出来的，因为主线程运行的时候，number的值为0，所以一直在循环
        // 如果能输出这句话，说明AAA线程在睡眠3秒后，更新的number的值，重新写入到主内存，并被main线程感知到了
        System.out.println(Thread.currentThread().getName() + "\t mission is over");

        /**
         * 运行结果:
         * AA	 come in
         * AA	 update number value:60
         * 但是最后的输出 不会输出 main 线程不知道结果修改为60了.这就是没加Volatile展示的现象.
         */
    }
}
