package com.study.knowlages.juc.atomics;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicMarkableReference;

/**
 * @Author wangziyu1
 * @Date 2022/4/7 15:04
 * @Version 1.0
 */
public class AtomicMarkableReferenceDemo {

    //
    static AtomicMarkableReference atomicMarkableReference = new AtomicMarkableReference(100, false);

    public static void main(String[] args) {

        new Thread(() -> {
            //首次查看默认的标识 应该为false
            boolean marked = atomicMarkableReference.isMarked();
            System.out.println(Thread.currentThread().getName() + "\t 默认标识位的值为:" + marked);
            atomicMarkableReference.compareAndSet(100, 101, marked, !marked);
            //暂停一秒保证其他线程可以获取到标志位
            try {
                TimeUnit.SECONDS.sleep(1L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + "\t 修改完成之后的标识位的值为" + atomicMarkableReference.isMarked());
        }, "t1").start();


        new Thread(() -> {
            //首次查看默认的标识 应该为false
            boolean marked = atomicMarkableReference.isMarked();
            System.out.println(Thread.currentThread().getName() + "\t 默认标识位的值为:" + marked);
            try {
                TimeUnit.SECONDS.sleep(2L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            boolean b = atomicMarkableReference.compareAndSet(100, 20220407, marked, !marked);

            System.out.println(Thread.currentThread().getName() + "\t 修改是否成功" + b);
        }, "t2").start();

    }
}
