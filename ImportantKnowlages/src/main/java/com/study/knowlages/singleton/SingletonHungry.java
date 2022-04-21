package com.study.knowlages.singleton;

/**
 * 饿汉试单例模式
 * 直接创建单例对象 就是饿汉试 比较饿比较着急,所以先创建出来.
 * 1.构造器私有化
 * 2.自行创建 使用静态变量
 * 3.向外提供这个实例
 * 4.强调使用的是单例不能修改,使用Final关键字
 */
public class SingletonHungry {

    /**
     *  * 4.强调使用的是单例不能修改,使用Final关键字
     */
    public static final SingletonHungry INSTANCE = new SingletonHungry();

    /**
     *  * 1.构造器私有化
     */
    private SingletonHungry() {
    }
}
