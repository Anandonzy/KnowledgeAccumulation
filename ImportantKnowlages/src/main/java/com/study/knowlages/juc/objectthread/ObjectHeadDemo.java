package com.study.knowlages.juc.objectthread;

import org.openjdk.jol.info.ClassLayout;

/**
 * @Author wangziyu1
 * @Date 2022/4/14 15:45
 * @Version 1.0
 * 查看Object 对象里面的字节分布情况
 * 占多少字节等
 *
 * 官网：http://openjdk.java.net/projects/code-tools/jol/
 * 定位：分析对象在JVM的大小和分布
 * -->
 * <dependency>
 *     <groupId>org.openjdk.jol</groupId>
 *     <artifactId>jol-core</artifactId>
 *     <version>0.9</version>
 * </dependency>
 *
 */
public class ObjectHeadDemo {
    public static void main(String[] args)
    {
        MyObject object = new MyObject();

        //引入了JOL，直接使用
        System.out.println(ClassLayout.parseInstance(object).toPrintable());

        //java5之前 只有重量级锁
        new Thread(() -> {
            synchronized (object){
                System.out.println("----hello juc");
            }
        },"t1").start();
    }
}

class MyObject{
    //int i =25;
    //boolean flag = false;
    //long aa = 55;

}
