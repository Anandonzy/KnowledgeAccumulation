package com.study.knowlages.stringpool;

/**
 * @Author wangziyu1
 * @Date 2022/3/16 10:47 上午
 * @Version 1.0
 * 字符串常量池的案例
 *
 * alibb
 * alibb
 * true
 *
 * java
 * java
 * false
 *
 * 释义:
 * 其实java在娘胎里面 sun.misc.Version 这个类就进入常量池了.
 * System代码解析 System -> initializeSystemClass() -> Version
 */
public class StringInternDemo {


    public static void main(String[] args) {

        String str1 = new StringBuilder("ali").append("bb").toString();
        System.out.println(str1);
        System.out.println(str1.intern());
        System.out.println(str1.intern() == str1);

        System.out.println();
        String str2 = new StringBuilder("ja").append("va").toString();
        System.out.println(str2);
        System.out.println(str2.intern());
        System.out.println(str2.intern() == str2);

    }
}
