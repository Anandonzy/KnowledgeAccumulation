package com.study.knowlages.juc.gc;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author wangziyu1
 * @Date 2022/3/10 4:37 下午
 * @Version 1.0
 * 演示 GC(Serial) 垃圾回收器
 * -Xms10m -Xmx10m -XX:PrintGCDetails -XX:+PrintConmandLineFlags -XX:+UseSerialGC
 */
public class GCDemo {


    public static void main(String[] args) throws InterruptedException {
        System.out.println("**使用SerialGC 垃圾回收器");
        Thread.sleep(Integer.MAX_VALUE);
        ConcurrentHashMap<Object, Object> map = new ConcurrentHashMap<>();


    }
}
