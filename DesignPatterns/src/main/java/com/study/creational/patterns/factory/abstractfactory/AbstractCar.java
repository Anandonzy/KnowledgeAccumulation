package com.study.creational.patterns.factory.abstractfactory;

/**
 * @author wangziyu
 * @version 1.0
 * @since 2023/8/3 16:33
 * 怎么把东西提升一个层次,定义抽象,(抽象类 接口)
 * 对车的功能进行抽象
 */
public abstract class AbstractCar {

    /**
     * 发动机
     */
    public String engine;

    /**
     * 抽象方法
     */
    public abstract void run();

}
