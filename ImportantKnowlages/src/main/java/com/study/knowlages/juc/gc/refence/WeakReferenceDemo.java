package com.study.knowlages.juc.gc.refence;

import java.lang.ref.WeakReference;

/**
 * @Author wangziyu1
 * @Date 2022/3/7 7:24 下午
 * @Version 1.0 弱引用
 * 只要gc 就会回收
 */
public class WeakReferenceDemo {

    public static void main(String[] args) {
        Object o1 = new Object();
        WeakReference<Object> weakReference = new WeakReference<>(o1);
        System.out.println(o1);
        System.out.println(weakReference.get());
        o1 = null;
        System.gc();
        System.out.println(o1);
        System.out.println(weakReference.get());
    }
}
