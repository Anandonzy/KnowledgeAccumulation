package com.study.knowlages.test;

import java.util.HashSet;

/**
 * @Author wangziyu1
 * @Date 2022/7/13 19:18
 * @Version 1.0
 */
public class Email {

    private String add;

    public Email(String add) {
        this.add = add;
    }

  /*  public int hashCode(){
        return add.hashCode();
    }*/

    public static void main(String[] args) {
        HashSet<Email> set = new HashSet<>();

        Email email = new Email("huawei.com");

        set.add(email);
        email.add = "aa";
        System.out.println(set.contains(email));

    }
}
