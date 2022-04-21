package com.study.knowlages.juc.gc;

/**
 * @Author wangziyu1
 * @Date 2022/3/7 11:38 上午
 * @Version 1.0
 */
public class GCRootDemo {


    // 方法区中的类静态属性引用的对象
//     private static GCRootDemo t2;

    // 方法区中的常量引用，GC Roots 也会以这个为起点，进行遍历
//     private static final GCRootDemo3 t3 = new GCRootDemo3(8);

    public static void m1() {
        // 第一种，虚拟机栈中的引用对象
        GCRootDemo t1 = new GCRootDemo();
        System.gc();
        System.out.println("第一次GC完成");
    }
    public static void main(String[] args) {
        m1();
    }
}
