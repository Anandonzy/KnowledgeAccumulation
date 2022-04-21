package com.study.knowlages.juc.myvolatile;

/**
 * Volatile Java虚拟机提供的轻量级同步机制
 * <p>
 * 可见性（及时通知）
 * 不保证原子性
 * 禁止指令重排
 * <p>
 * 1.验证volatile的 不保证原子性
 * 1.1 什么是原子性:不可分割，完整性，也就是说某个线程正在做某个具体业务时，中间不可以被加塞或者被分割，需要具体完成，要么同时成功，要么同时失败。
 * 数据库也经常提到事务具备原子性
 *
 * happens-before
 * 1.次序原则
 *
 *   一个线程内，按照代码顺序，写在前面的操作先行发生于写在后面的操作；
 *
 * 2.锁定规则
 *
 *   一个`unlock`在时间上现行发生对一个锁lock 的操作
 *
 *     同一把锁只有上个线程解锁了,后面的线程才能使用
 *
 * 3.volatile变量规则
 *
 *   volatile修饰的变量写操作一定发生在读操作之前,并且对另外一个操作可见.
 *
 * 4.传递规则
 *
 *   如果操作A先行发生于操作B，而操作B又先行发生于操作C，则可以得出操作A先行发生于操作C；
 *
 * 5.线程启动规则(Thread Start Rule)
 *
 *   Thread对象的start()方法先行发生于此线程的每一个动作
 *
 * 6.线程中断规则(Thread Interruption Rule)
 *
 *   对线程interrupt()方法的调用先行发生于被中断线程的代码检测到中断事件的发生；
 *
 * 7.线程终止规则(Thread Termination Rule)
 *
 *   线程中的所有操作都先行发生于对此线程的终止检测，我们可以通过Thread::join()方法是否结束、Thread::isAlive()的返回值等手段检测线程是否已经终止执行。
 * 线程的状态一定是最后才是终止.
 *
 * 8.对象终结规则(Finalizer Rule)
 *
 *   对象没有完成初始化之前，是不能调用finalized()方法的
 *
 *
 *   4大内存屏障
 *   LoadLoad
 *   StoreStore
 *   LoadStore
 *   StoreLoad
 *   写:
 *
 * 1. 在每个 volatile 写操作的前⾯插⼊⼀个 StoreStore 屏障
 * 2. 在每个 volatile 写操作的后⾯插⼊⼀个 StoreLoad 屏障
 *
 * 读:
 *
 * 1. 在每个 volatile **读操作**的后⾯插⼊⼀个 LoadStore 屏障
 * 2. 在每个 volatile 读操作的后⾯插⼊⼀个 LoadLoad 屏障
 */

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 假设是主物理内存
 */
class MyData {
    /**
     * volatile 修饰的关键字，是为了增加 主线程和线程之间的可见性，只要有一个线程修改了内存中的值，其它线程也能马上感知
     */
    volatile int number = 0;

    public void addTo60() {
        this.number = 60;
    }

    /**
     * 注意，此时number 前面是加了volatile修饰
     */
    public void addPlusPlus() {
        number++;
    }


    AtomicInteger atomicInteger = new AtomicInteger();

    public void addAtomic() {
        // 相当于 atomicInter ++
        atomicInteger.getAndIncrement();
    }
}

public class VolatileDemo2 {

    public static void main(String[] args) {

        MyData myData = new MyData();

        //20个线程 每个线程+1 加1000次
        for (int i = 0; i < 20; i++) {
            new Thread(() -> {
                // 里面
                for (int j = 0; j < 1000; j++) {
                    myData.addPlusPlus();
                    myData.addAtomic();
                }
            }, String.valueOf(i)).start();
        }
        // 需要等待上面20个线程都计算完成后，在用main线程取得最终的结果值
        // 这里判断线程数是否大于2，为什么是2？因为默认是有两个线程的，一个main线程，一个gc线程
        while(Thread.activeCount() > 2) {
            // yield表示不执行
            Thread.yield();
        }

        // 查看最终的值
        // 假设volatile保证原子性，那么输出的值应该为：  20 * 1000 = 20000
        System.out.println(Thread.currentThread().getName() + "\t finally number value: " + myData.number);
        System.out.println(Thread.currentThread().getName() + "\t atomicNumber number value:" + myData.atomicInteger);

    }
}
