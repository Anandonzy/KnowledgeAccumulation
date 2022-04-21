package com.study.knowlages.juc.objectthread;

import org.openjdk.jol.info.ClassLayout;

/**
 * @Author wangziyu1
 * @Date 2022/4/15 10:57
 * @Version 1.0
 */
public class synchronizedUpgradeDemo {
    public static void main(String[] args)
    {

    }

    /**
     * 无锁情况演示 查看markword情况
     */
    private static void noLock() {
        Object o = new Object();
        System.out.println("10进制hash码："+o.hashCode());//10进制
        System.out.println("16进制hash码："+Integer.toHexString(o.hashCode()));
        System.out.println("2进制hash码："+Integer.toBinaryString(o.hashCode()));
        //00100011111111000110001001011110
        //  100011111111000110001001011110

        System.out.println( ClassLayout.parseInstance(o).toPrintable());
    }

    /**
     * 偏向锁演示
     * 需要开启jvm参数
     *  -XX:+UseBiasedLocking  开启偏向锁(默认)
     *  -XX:-UseBiasedLocking   关闭偏向锁
     *  -XX:BiasedLockingStartupDelay=0             关闭延迟(演示偏向锁时需要开启)
     *
     *  参数说明：偏向锁在JDK1.6以上默认开启，开启后程序启动几秒后才会被激活，
     *  可以使用JVM参数来关闭延迟 -XX:BiasedLockingStartupDelay=0 如果确定锁通常处于竞争状态则可通过JVM参数 -XX:-UseBiasedLocking 关闭偏向锁，那么默认会进入轻量级锁
     *  关闭延时参数，启用该功能
     */
    public static void biasedLock()
    {
        Object o = new Object();

        new Thread(() ->
        {
            synchronized (o)
            {
                System.out.println( ClassLayout.parseInstance(o).toPrintable());
            }

        },"t1").start();
    }

}
