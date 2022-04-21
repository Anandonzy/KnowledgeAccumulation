package com.study.knowlages.juc.gc.refence;

/**
 * @Author wangziyu1
 * @Date 2022/3/7 6:55 下午
 * @Version 1.0
 * 强软弱虚引用
 */
public class StrongReferenceDemo {


    public static void main(String[] args) {

        // 这样定义的默认就是强应用
        Object obj1 = new Object();

        // 使用第二个引用，指向刚刚创建的Object对象
        Object obj2 = obj1;

        // 置空
        obj1 = null;

        // 垃圾回收
        System.gc();

        System.out.println(obj1);

        System.out.println(obj2);//强引用 还有引用就不回收

    }
}
