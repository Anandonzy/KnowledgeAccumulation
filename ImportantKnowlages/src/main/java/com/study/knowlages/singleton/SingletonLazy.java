package com.study.knowlages.singleton;

/**
 * 1、构造器私有化
 * 2、用一个静态变量保存这个唯一的实例
 * 3、提供一个静态方法，获取这个实例对象
 *
 * 线程不安全
 */
public class SingletonLazy {


    static SingletonLazy instance;
    private SingletonLazy() {}

    public static SingletonLazy getInstance(){
        if (instance == null) {
            instance = new SingletonLazy();
        }
        return instance;
    }

    public static void main(String[] args) {
        //演示线程不安全的例子


    }
}
