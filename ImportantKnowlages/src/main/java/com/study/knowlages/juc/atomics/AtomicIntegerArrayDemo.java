package com.study.knowlages.juc.atomics;

import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * @Author wangziyu1
 * @Date 2022/4/7 14:53
 * @Version 1.0
 * AtomicIntegerArray演示
 */
public class AtomicIntegerArrayDemo {


    public static void main(String[] args) {

        //初始化
        AtomicIntegerArray atomicIntegerArray = new AtomicIntegerArray(new int[5]);
        //AtomicIntegerArray atomicIntegerArray = new AtomicIntegerArray(5);
        //AtomicIntegerArray atomicIntegerArray = new AtomicIntegerArray(new int[]{1, 2, 3, 4, 5});

        for (int i = 0; i < atomicIntegerArray.length(); i++) {
            System.out.println(atomicIntegerArray.get(i));
        }

        System.out.println();
        System.out.println();
        System.out.println();
        int tmpInt = 0;
        tmpInt = atomicIntegerArray.getAndSet(0, 1122);
        System.out.println(tmpInt+"\t"+atomicIntegerArray.get(0));
        atomicIntegerArray.getAndIncrement(1);
        atomicIntegerArray.getAndIncrement(1);
        tmpInt = atomicIntegerArray.getAndIncrement(1);
        System.out.println(tmpInt+"\t"+atomicIntegerArray.get(1));



    }

}
