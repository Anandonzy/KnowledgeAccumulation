package com.study.knowlages.singleton;

/**
 * @Author wangziyu1
 * @Date 2022/4/1 15:02
 * @Version 1.0
 * 由于传统的单例模式多线程下会存在问题 所以需要使用volatile修饰 禁止指令重排
 * 经典单例模式 双端锁
 * 另外一种方式不使用volatile方式写单例模式
 * 使用静态内部类
 * StaticSingltonDemo
 */
public class DoubleCheckSingletonDemo {

    static volatile DoubleCheckSingletonDemo doubleCheckSingletonDemo =null;

    /**
     * 私有化构造
     */
    private DoubleCheckSingletonDemo() {
    }

    public static DoubleCheckSingletonDemo getInstance() {

        if (doubleCheckSingletonDemo == null) {
            synchronized (DoubleCheckSingletonDemo.class){
                if(doubleCheckSingletonDemo ==null){
                    doubleCheckSingletonDemo = new DoubleCheckSingletonDemo();
                }
            }
        }
        return doubleCheckSingletonDemo;
    }
}
