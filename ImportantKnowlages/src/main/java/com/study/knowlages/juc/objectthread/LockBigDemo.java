package com.study.knowlages.juc.objectthread;

/**
 * @Author wangziyu1
 * @Date 2022/4/15 11:51
 * @Version 1.0
 * 锁粗化
 * 假如方法中首尾相接，前后相邻的都是同一个锁对象，那JIT编译器就会把这几个synchronized块合并成一个大块，
 * 加粗加大范围，一次申请锁使用即可，避免次次的申请和释放锁，提升了性能
 */
public class LockBigDemo {
    static Object objectLock = new Object();

    public static void main(String[] args) {
        new Thread(() -> {
            synchronized (objectLock) {
                System.out.println("-------1");
            }
            synchronized (objectLock) {
                System.out.println("-------2");
            }
            synchronized (objectLock) {
                System.out.println("-------3");
            }
            synchronized (objectLock) {
                System.out.println("-------4");
            }

            //如果像上面一样这样写 JIT就会给你优化成这样 这就是锁粗化
            System.out.println("-------1");

            System.out.println("-------2");


            System.out.println("-------3");


            System.out.println("-------4");
        }, "t1").start();
    }
}
