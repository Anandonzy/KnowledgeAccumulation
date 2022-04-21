package com.study.knowlages.juc;

import lombok.Getter;
import lombok.Setter;

/**
 * @Author wangziyu1
 * @Date 2022/2/23 11:01 上午
 * @Version 1.0
 */


@Getter
@Setter
class Person {
    private Integer id;
    private String personName;

    public Person(String personName) {
        this.personName = personName;
    }
}
public class TransferValueDemo {

    public void changeValue1(int age) {
        age = 30;
    }

    public void changeValue2(Person person) {
        person.setPersonName("aaa");
    }
    public void changeValue3(String str) {
        str = "XXX";
    }

    public static void main(String[] args) {
        TransferValueDemo test = new TransferValueDemo();

        // 定义基本数据类型
        int age = 20;
        test.changeValue1(age);//在栈里面改为30  但是底下输出的是main里面的age
        System.out.println("age ----" + age);  //20

        // 实例化person类
        Person person = new Person("abc");
        test.changeValue2(person);
        System.out.println("personName-----" + person.getPersonName());  //xxx

        // String
        String str = "abc";
        test.changeValue3(str);
        System.out.println("string-----" + str); //abc

    }

}
