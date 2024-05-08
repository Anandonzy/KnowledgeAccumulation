package com.study.creational.patterns.factory.abstractfactory;

import com.study.creational.patterns.factory.abstractfactory.WuliFactory;
import com.study.creational.patterns.factory.abstractfactory.car.VanCar;

/**
 * @author wangziyu
 * @version 1.0
 * @since 2023/8/14 19:04
 * 五菱第二个工厂
 * vanCar
 *
 */
public  class WuliVanCarFactory extends WuliFactory {
    public AbstractCar newCar() {
        return new VanCar();
    }

    public AbstractMask newMask() {
        return null;
    }
}
