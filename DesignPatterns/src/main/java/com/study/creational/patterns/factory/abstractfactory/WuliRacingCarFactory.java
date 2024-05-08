package com.study.creational.patterns.factory.abstractfactory;

import com.study.creational.patterns.factory.abstractfactory.car.MiniCar;
import com.study.creational.patterns.factory.facotrymethod.AbstractFactory;

/**
 * @author wangziyu
 * @version 1.0
 * @since 2023/8/14 19:01
 * 具体工厂 -> 继承五菱工厂
 */
public class WuliRacingCarFactory extends WuliCarFactory {
    public AbstractCar newCar() {
        return new MiniCar();
    }
}
