package com.study.creational.patterns.singleton;

import java.util.Map;
import java.util.Properties;

/**
 * @author wangziyu
 * @version 1.0
 * @since 2023/7/21 16:38
 */
public class SingletDemo {

    public static void main(String[] args) {


        Person person = Person.getInstance();
        Person person1 = Person.getInstance();
        //看是否是一个对象
        System.out.println(person1 == person);

        Properties properties = System.getProperties();
        System.out.println(properties);

        //获取当前系统环境信息
        Map<String, String> getenv = System.getenv();
        System.out.println(getenv);
    }
}
