package com.study.knowlages.juc.gc;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author wangziyu1
 * @Date 2022/3/8 11:12 上午
 * @Version 1.0
 * GC 回收超时
 * JVM参数配置: -Xms10m -Xmx10m -XX:+PrintGCDetails -XX:MaxDirectMemorySize=5m
 */
public class GCOverheadLimitDemo {
    public static void main(String[] args) {
        int i = 0;
        List<String> list = new ArrayList<>();
        try {
            while (true) {
                list.add(String.valueOf(++i).intern());
            }
        } catch (Exception e) {
            System.out.println("***************i:" + i);
            e.printStackTrace();
            throw e;
        } finally {

        }

    /*    int i = 0;
        List<String> list = new ArrayList<>();
        list.add(String.valueOf(++i).intern());
        System.out.println(list.toString());*/
    }
}
