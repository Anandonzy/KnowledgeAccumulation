package com.study.creational.patterns.factory.simplefactory;

/**
 * @author wangziyu
 * @version 1.0
 * @since 2023/8/3 16:32
 */
public class SimpleFactoryDemo {

    public static void main(String[] args) {

        WulingFactroy wulingFactroy = new WulingFactroy();
        AbstractCar mini = wulingFactroy.newCar("mini");
        AbstractCar van = wulingFactroy.newCar("van");
        AbstractCar zz = wulingFactroy.newCar("zz");
        mini.run();
        van.run();
        System.out.println(mini);
        System.out.println(van);
        System.out.println(zz);


    }
}
