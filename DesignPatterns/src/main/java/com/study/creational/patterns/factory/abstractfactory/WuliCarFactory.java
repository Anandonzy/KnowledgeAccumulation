package com.study.creational.patterns.factory.abstractfactory;

/**
 * @author wangziyu
 * @version 1.0
 * @since 2023/8/14 19:30
 * 汽车集团在抽象
 */
public abstract class WuliCarFactory extends WuliFactory{

    public abstract AbstractCar newCar();

    public AbstractMask newMask() {
        return null;
    }
}
