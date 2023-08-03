package com.study.creational.patterns.factory.facotrymethod;

/**
 * @author wangziyu
 * @version 1.0
 * @since 2023/8/3 16:37
 */

/**
 * 具体产品
 */
public class VanCar extends AbstractCar {

    public VanCar() {
        this.engine = "单杠柴油机";
    }

    public void run() {
        System.out.println(engine + "-->哒哒哒!");
    }
}
