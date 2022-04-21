package com.study.knowlages.juc.lock;

/**
 * @Author wangziyu1
 * @Date 2022/3/30 16:14
 * @Version 1.0
 * 从字节码角度分析synchronized实现
 */
public class LockByteCodeDemo {

    Object o = new Object();
    public void m1(){
        synchronized (o){
            System.out.println("----hello sync");
            throw new RuntimeException("121231");
        }
    }


    public static void main(String[] args) {

        System.out.println(11);



    }

}
