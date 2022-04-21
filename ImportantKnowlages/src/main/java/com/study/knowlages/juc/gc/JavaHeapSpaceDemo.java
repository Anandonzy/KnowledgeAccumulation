package com.study.knowlages.juc.gc;

/**
 * @Author wangziyu1
 * @Date 2022/3/8 11:07 上午
 * @Version 1.0
 */
public class JavaHeapSpaceDemo {

    public static void main(String[] args) {

        // 堆空间的大小 -Xms10m -Xmx10m
        // 创建一个 80M的字节数组
        byte [] bytes = new byte[80 * 1024 * 1024];
    }
}
