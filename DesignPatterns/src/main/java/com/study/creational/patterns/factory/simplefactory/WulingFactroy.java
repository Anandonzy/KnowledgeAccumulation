package com.study.creational.patterns.factory.simplefactory;

/**
 * @author wangziyu
 * @version 1.0
 * @since 2023/8/3 16:36
 * 简单工厂
 */
public class WulingFactroy {

    //简单工厂的核心 就直接根据类型创建即可.
    public AbstractCar newCar(String type){

        //核心方法一切要从简
        if("van".equals(type)){
            return new VanCar();
        }else if("mini".equals(type)){
            return new MiniCar();
        }
        //违反了开闭原则,不能拓展
        return null;
    }
}
