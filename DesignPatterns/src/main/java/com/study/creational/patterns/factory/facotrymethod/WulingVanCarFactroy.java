package com.study.creational.patterns.factory.facotrymethod;

/**
 * @author wangziyu
 * @version 1.0
 * @since 2023/8/3 17:21
 */
public class WulingVanCarFactroy extends AbstractFactory{
    public AbstractCar newCar() {
        return new VanCar();
    }
}
