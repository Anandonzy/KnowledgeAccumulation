package com.study.creational.patterns.singleton;

/**
 * @author wangziyu
 * @version 1.0
 * @since 2023/7/17 10:19
 */
public class Person {

    private String name;
    private String age;

    //懒汉 饿汉
    private static volatile Person instance; //内存可见性

    /**
     * 私有化构造器
     */
    private Person() {

    }

    /**
     * 通过自定义的方法向外获取对象
     * 需要锁住不然懒汉试就会出现线程安全的问题.
     * 1.synchronized 如果锁方法 锁太大
     * @return
     */
    public  static Person getInstance() {
        if (instance == null) {
            synchronized(Person.class){
                if(instance == null){
                    //DCL双重检测机制+volatile内存可见性
                    Person person = new Person(); //需要加 volatile 内存可见性
                    //多线程安全问题
                    instance = person;
                }
            }
        }
        return instance;
    }
}
