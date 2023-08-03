package com.study.creational.patterns.prototype;

/**
 * @author wangziyu
 * @version 1.0
 * @since 2023/7/21 17:00
 * 实现Cloneable接口的clone方法
 */
public class User implements Cloneable {
    private String name;
    private String age;

    public User() {
        System.out.println("创建user对象!");
    }

    public User(String name, String age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    /**
     * 重新创建一个人 附于所有的属性
     * @return
     * @throws CloneNotSupportedException
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        User user = new User();
        user.setName(this.name);
        user.setAge(this.age);
        return user;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", age='" + age + '\'' +
                '}';
    }
}
