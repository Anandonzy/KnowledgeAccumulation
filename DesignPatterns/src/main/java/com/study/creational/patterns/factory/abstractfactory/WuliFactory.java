package com.study.creational.patterns.factory.abstractfactory;

/**
 * @author wangziyu
 * @version 1.0
 * @since 2023/8/14 19:00
 * 五菱 想做好几个事情
 * 总厂的工厂规范.
 */
public abstract class WuliFactory {
    /**
     * 总厂先定义能干那些事情.
     *
     * @return
     */
    public abstract AbstractCar newCar();

    public abstract AbstractMask newMask();
}
