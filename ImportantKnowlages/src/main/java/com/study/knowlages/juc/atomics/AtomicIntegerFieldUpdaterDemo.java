package com.study.knowlages.juc.atomics;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * @Author wangziyu1
 * @Date 2022/4/7 16:07
 * @Version 1.0
 * <p>
 * AtomicIntegerFieldUpdater 以一种线程安全的方式修改资源类
 */

class BankAccount {
    String bankName = "bbc";

    //1.以一种线程安全的方式操作非线程安全的对象的某些字段
    //更新对象的属性必须使用 public volatile 修饰符
    public volatile int money = 0;

    //2.因为对象的属性修改类原子类都是抽象类,所以每次都必须使用
    //惊天方法的newUpdater()创建一个更新器,并且需要设置想要更新的类和属性
    AtomicIntegerFieldUpdater fieldUpdater = AtomicIntegerFieldUpdater.newUpdater(BankAccount.class, "money");

    //不加锁+性能高，局部微创
    public void transfer(BankAccount bankName) {
        fieldUpdater.incrementAndGet(bankName);
    }
}

/**
 * 以一种线程安全的方式操作非线程安全对象的某些字段。
 * 需求：
 * 1000个人同时向一个账号转账一元钱，那么累计应该增加1000元，
 * 除了synchronized和CAS,还可以使用AtomicIntegerFieldUpdater来实现。
 */
public class AtomicIntegerFieldUpdaterDemo {

    public static void main(String[] args) {
        BankAccount bankAccount = new BankAccount();
        for (int i = 1; i <= 1000; i++) {
            new Thread(() -> {
                bankAccount.transfer(bankAccount);
            }, String.valueOf(i)).start();
        }
        try {
            TimeUnit.SECONDS.sleep(1L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(Thread.currentThread().getName() + "\t ---bankcount" + bankAccount.money);
    }
}
