package com.study.knowlages.juc.cas;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Author wangziyu1
 * @Date 2022/2/21 5:06 下午
 * @Version 1.0 cas 进入的 aba的问题
 * 使用原子引用就可以解决aba问题
 */
class User {
    private String name;
    private Integer age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public User(String name, Integer age) {
        this.name = name;
        this.age = age;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}

public class AtomicReferenceDemo {
    public static void main(String[] args) {
        AtomicInteger atomicInteger = new AtomicInteger();
        AtomicReference<User> userAtomicReference = new AtomicReference<>();

        User z3 = new User("aa", 13);
        User li4 = new User("bb", 25);
        userAtomicReference.set(z3);

        System.out.println(userAtomicReference.compareAndSet(z3, li4) +"\t "+userAtomicReference.get().toString());
        System.out.println(userAtomicReference.compareAndSet(li4, z3) +"\t "+userAtomicReference.get().toString());

    }
}
