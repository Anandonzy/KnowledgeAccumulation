package com.study.knowlages.juc.lock.rwlock;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @Author wangziyu1
 * @Date 2022/4/19 11:34
 * @Version 1.0
 * 读写锁 锁降级
 * 简单的说就是
 *
 * **锁降级**：将写入锁降级为读锁(类似Linux文件读写权限理解，就像写权限要高于读权限一样)
 *
 * **写的权限高 可以降级到写**
 *
 * **读的权限低 不能升级为写锁**
 *
 *
 * **锁降级**：遵循获取写锁→再获取读锁→再释放写锁的次序，写锁能够降级成为读锁。
 *
 * 如果一个线程占有了写锁，在不释放写锁的情况下，它还能占有读锁，即写锁降级为读锁。
 *
 * 结论:
 * 如果有线程在读，那么写线程是无法获取写锁的，是悲观锁的策略
 */
public class LockDownGradingDemo {

    public static void main(String[] args) {
        ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
        ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();
        //有且只有一个线程 验证锁降级 先写后读 可以
        writeLock.lock();
        System.out.println("-------正在写入");
        readLock.lock();
        System.out.println("-------正在读取");
        writeLock.unlock();
        readLock.unlock();
        //先读 在写的话就会一直等待 不会产生锁降级
      /*  readLock.lock();
        System.out.println("-------正在读取");
        writeLock.lock();
        System.out.println("-------正在写入");
        readLock.unlock();
        writeLock.unlock();
*/


    }
}
