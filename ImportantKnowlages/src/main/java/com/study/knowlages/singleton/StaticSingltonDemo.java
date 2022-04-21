package com.study.knowlages.singleton;

/**
 * @Author wangziyu1
 * @Date 2022/4/1 15:10
 * @Version 1.0
 * 面试题，反周志明老师的案例，你还有不加volatile的方法吗
 * 使用静态内部类的方式实现
 */
public class StaticSingltonDemo {

    private StaticSingltonDemo() {
    }

    private static class SingletinDemoHandler{
        private static StaticSingltonDemo instance = new StaticSingltonDemo();
    }

    public static StaticSingltonDemo getInstance(){
        return SingletinDemoHandler.instance;
    }
}
