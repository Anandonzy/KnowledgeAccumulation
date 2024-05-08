package com.study.creational.patterns.factory.abstractfactory;

/**
 * @author wangziyu
 * @version 1.0
 * @since 2023/8/14 19:18
 * 抽象出来 抽象成接口(只有方法)  抽象类(有些属性需要传递下去的话!)
 */
public class MainTest {

    public static void main(String[] args) {

        //拿的杭州口罩厂子
        WuliHangzhouMaskFactory wuliHangzhouMaskFactory = new WuliHangzhouMaskFactory();
        //口罩能取到
        AbstractMask hangzhouMask = wuliHangzhouMaskFactory.newMask();

        //汽车取不到
        AbstractCar hangzhouCar = wuliHangzhouMaskFactory.newCar();
        hangzhouMask.proteteMe();
//        hangzhouCar.run();//取不到.

        //拿一个武汉口罩厂子
        WuliWuhanMaskFactory wuhanFactory = new WuliWuhanMaskFactory();
        AbstractMask wuhanMask = wuhanFactory.newMask();
        wuhanMask.proteteMe();


    }
}
