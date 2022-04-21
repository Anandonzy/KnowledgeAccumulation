package com.study.knowlages.juc;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author wangziyu1
 * @Date 2022/2/23 11:28 上午
 * @Version 1.0 锁演示
 * ReentrantLock 默认非公平锁
 */
public class T1 {
    public static void main(String[] args) {

        /**
         * 创建一个可重入锁，true 表示公平锁，false 表示非公平锁。默认非公平锁
         */
        Lock lock = new ReentrantLock();

    }
}
