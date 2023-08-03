package com.study.knowlages.test;

import java.util.ArrayList;

/**
 * @Author wangziyu1
 * @Date 2022/7/6 10:14
 * @Version 1.0
 */
public class ArrayListTest {

    public static void main(String[] args) {
        ArrayList<Integer> list2= new ArrayList<>();
        list2.add(2);
        list2.add(2);
        list2.add(2);

        ArrayList<Integer> list = new ArrayList<>(list2);
        list.add(1);
        list.add(1);
        list.add(1);
        list.add(1);
        System.out.println(list);
    }
}
