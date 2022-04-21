package com.study.knowlages.juc.gc;

import java.nio.ByteBuffer;

/**
 * @Author wangziyu1
 * @Date 2022/3/8 11:32 上午
 * @Version 1.0
 * 一句话说：本地内存不足，但是堆内存充足的时候，就会出现这个问题
 * 演示jum直接内存溢出
 * 设置jvm参数 -Xms10m -Xmx10m -XX:+PrintGCDetails -XX:MaxDirectMemorySize=5m
 */
public class DirectBufferMemoryDemo {
    public static void main(String[] args) {

        // 只设置了5M的物理内存使用，但是却分配 6M的空间
        ByteBuffer bb = ByteBuffer.allocateDirect(6 * 1024 * 1024);
    }

}
