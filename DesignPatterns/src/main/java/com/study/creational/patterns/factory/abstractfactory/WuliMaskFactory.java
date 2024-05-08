package com.study.creational.patterns.factory.abstractfactory;

/**
 * @author wangziyu
 * @version 1.0
 * @since 2023/8/14 19:34
 * 五菱口罩集团
 */

public abstract class WuliMaskFactory extends  WuliFactory{
    public AbstractCar newCar() {
        return null;
    }

    public abstract AbstractMask newMask() ;
}
