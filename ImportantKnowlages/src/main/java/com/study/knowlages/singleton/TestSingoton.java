package com.study.knowlages.singleton;

public class TestSingoton {

    public static void main(String[] args) {


        SingletonHungryEnum instance = SingletonHungryEnum.INSTANCE;
        System.out.println(instance.toString());

        SingletonHungry instance1 = SingletonHungry.INSTANCE;
        SingletonHungry instance2 = SingletonHungry.INSTANCE;

        //可以看到new出来额是一个对象
        System.out.println(instance1.toString());
        System.out.println(instance2.toString());

    }
}
