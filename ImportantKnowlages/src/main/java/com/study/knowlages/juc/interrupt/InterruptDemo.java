package com.study.knowlages.juc.interrupt;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author wangziyu1
 * @Date 2022/3/31 10:43
 * @Version 1.0
 * 线程中断标志位
 * 在需要中断的线程中不断监听中断状态，
 * 一旦发生中断，就执行相应的中断处理业务逻辑。
 * 1.通过volatile中断
 * 2.通过AtomicBoolean中断
 * 3.使用Interrupt中断
 * 总结:
 * interrupt()方法是一个实例方法
 * 它通知目标线程中断，也就是设置目标线程的中断标志位为true，中断标志位表示当前线程已经被中断了。
 *
 * isInterrupted()方法也是一个实例方法
 * 它判断当前线程是否被中断（通过检查中断标志位）并获取中断标志
 *
 * Thread类的静态方法interrupted()
 * 返回当前线程的中断状态(boolean类型)且将当前线程的中断状态设为false，此方法调用之后会清除当前线程的中断标志位的状态（将中断标志置为false了），返回当前值并清零置false
 */
public class InterruptDemo {

    static volatile Boolean isStop = false;
    static AtomicBoolean atomicBoolean = new AtomicBoolean(false);

    public static void main(String[] args) {

        //静态方法的interrupted 会返回当前值并且清零置位false
        System.out.println(Thread.currentThread().getName()+"---"+Thread.interrupted());
        System.out.println(Thread.currentThread().getName()+"---"+Thread.interrupted());
        System.out.println("111111");
        Thread.currentThread().interrupt();///----false---> true
        System.out.println("222222");
        System.out.println(Thread.currentThread().getName()+"---"+Thread.interrupted()); //会清理状态为false 所以底下会为false
        System.out.println(Thread.currentThread().getName()+"---"+Thread.interrupted());
    }

    /**
     * 当使用sleep/wait等方法的时候如果有InterruptedException 在try/catch捕获异常的时候加上Thread.currentThread().interrupt();
     * 否则线程不会被中断还会继续执行. 又一次证明了interrupt方法不是立刻停止线程的.
     */
    public static void m5() {
        Thread t1 = new Thread(() -> {
            while (true) {
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println("-----isInterrupted() = true，程序结束。");
                    break;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();//???????  //线程的中断标志位为false,无法停下，需要再次掉interrupt()设置true
                    e.printStackTrace();
                }
                System.out.println("------hello Interrupt");
            }
        }, "t1");
        t1.start();

        try { TimeUnit.SECONDS.sleep(3); } catch (InterruptedException e) { e.printStackTrace(); }

        new Thread(() -> {
            t1.interrupt();//修改t1线程的中断标志位为true
        },"t2").start();
    }

    /**
     * 中断为true后不是立停止线程的.
     */
    public static void m4() {
        //中断为true后不是立停止线程的.
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 300; i++) {
                System.out.println("------i: " + i);
            }
            System.out.println("t1.interrupt()调用之后02： " + Thread.currentThread().isInterrupted());
        }, "t1");
        t1.start();
        System.out.println("t1.interrupt()调用之前,t1线程的中断标识默认值： " + t1.isInterrupted());
        try {
            TimeUnit.MILLISECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //实例方法interrupt()仅仅是设置线程的中断状态位设置为true，不会停止线程
        t1.interrupt();
        //活动状态,t1线程还在执行中
        System.out.println("t1.interrupt()调用之后01： " + t1.isInterrupted());

        try {
            TimeUnit.MILLISECONDS.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //非活动状态,t1线程不在执行中，已经结束执行了。
        System.out.println("t1.interrupt()调用之后03： " + t1.isInterrupted());
    }


    /**
     * 使用interrupt和isinterrupt 配合使用中断线程.
     */
    public static void m3() {
        Thread t1 = new Thread(() -> {
            while (true) {
                if (Thread.currentThread().isInterrupted()) { //isInterrupted 默认为false
                    System.out.println("----isInterrupted =true,程序结束");
                    break;
                }
                System.out.println("----hello isInterrupted");
            }
        }, "t1");
        t1.start();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new Thread(() -> {
            t1.interrupt(); //修改t1的中断标志位 为true
        }, "t2").start();
    }


    /**
     * 使用AtomicBoolean 优雅的停止线程
     */
    public static void m2() {
        new Thread(() -> {
            while (true) {
                if (atomicBoolean.get()) {
                    System.out.println("----atomicBoolean =true,程序结束");
                    break;
                }
                System.out.println("---hello atomicBoolean");
            }
        }, "t1").start();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new Thread(() -> {
            atomicBoolean.set(true);
        }, "t2").start();
    }

    /**
     * 使用volatile优雅的停止线程
     */
    public static void m1() {
        new Thread(() -> {
            while (true) {
                if (isStop == true) {
                    System.out.println("----isStop =true,程序结束");
                    break;
                }
                System.out.println("---hello isStop");
            }

        }, "AA").start();

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new Thread(() -> {
            isStop = true;
        }, "BB").start();
    }

}
