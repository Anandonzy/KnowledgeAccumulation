package com.study.creational.patterns.factory.simplefactory;

/**
 * @author wangziyu
 * @version 1.0
 * @since 2023/8/3 16:33
 */
public abstract class AbstractCar {

    /**
     * 发动机
     */
    String engine;

    /**
     * 抽象方法
     */
    public abstract void run();

}
