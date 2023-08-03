package com.study.creational.patterns.factory.facotrymethod;

/**
 * @author wangziyu
 * @version 1.0
 * @since 2023/8/3 16:39
 */
public class MiniCar extends AbstractCar {
    public MiniCar() {
        this.engine = "四缸水平对置发动机";
    }

    public void run() {
        System.out.println(engine+"-->> 嘟嘟!");
    }
}
