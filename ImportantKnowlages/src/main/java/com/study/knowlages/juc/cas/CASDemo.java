package com.study.knowlages.juc.cas;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author wangziyu1
 * @Date 2022/2/18 5:06 下午
 * @Version 1.0
 * AtomicInteger 的简单使用
 *
 */
public class CASDemo {

    public static void main(String[] args) {

        //初始化为5
        AtomicInteger atomicInteger = new AtomicInteger(5);
        atomicInteger.compareAndSet(5, 2022);
        System.out.println(atomicInteger.get());

        atomicInteger.compareAndSet(5, 1024);
        System.out.println(atomicInteger.get());
        atomicInteger.getAndIncrement();
    }
}
