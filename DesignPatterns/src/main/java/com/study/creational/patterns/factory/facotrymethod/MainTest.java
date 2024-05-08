package com.study.creational.patterns.factory.facotrymethod;

/**
 * @author wangziyu
 * @version 1.0
 * @since 2023/8/3 17:18
 */
public class MainTest {

    public static void main(String[] args) {


        //测试抽象工厂.
        AbstractFactory wulingMinCarFactroy = new WulingMinCarFactroy();
        //mini工厂的
        AbstractCar abstractCar = wulingMinCarFactroy.newCar();
        abstractCar.run();

        AbstractFactory vanCarFactroy = new WulingVanCarFactroy();
        //Van工厂的
        AbstractCar mincar = vanCarFactroy.newCar();
        mincar.run();

    }
}
