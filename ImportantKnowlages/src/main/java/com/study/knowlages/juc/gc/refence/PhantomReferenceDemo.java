package com.study.knowlages.juc.gc.refence;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.concurrent.TimeUnit;

/**
 * @Author wangziyu1
 * @Date 2022/3/7 7:46 下午
 * @Version 1.0
 * 虚引用 配合 引用队列使用ReferenceQueue
 * 软引用回收之后 才会放到ReferenceQueue队列里面.
 */
public class PhantomReferenceDemo {

    public static void main(String[] args) {

        Object o1 = new Object();

        // 创建引用队列
        ReferenceQueue<Object> referenceQueue = new ReferenceQueue<>();

        // 创建一个弱引用
        //WeakReference<Object> weakReference = new WeakReference<>(o1, referenceQueue);

        // 创建一个虚引用
        PhantomReference<Object> weakReference = new PhantomReference<>(o1, referenceQueue);

        System.out.println(o1);
        System.out.println(weakReference.get());
        // 取队列中的内容
        System.out.println(referenceQueue.poll());

        o1 = null;
        System.gc();
        System.out.println("执行GC操作");

        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(o1);
        System.out.println(weakReference.get());
        // 取队列中的内容
        System.out.println(referenceQueue.poll());

    }
}
