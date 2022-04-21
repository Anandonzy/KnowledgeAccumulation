package com.study.knowlages.juc.gc;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * @Author wangziyu1
 * @Date 2022/3/7 7:33 下午
 * @Version 1.0
 */
public class WeakHashMapDemo {

    public static void main(String[] args) {

//        myHashMap();
        myWeakHashMap();


    }

    private static void myHashMap() {
        Map<Integer, String> map = new HashMap<>();
        Integer key = new Integer(1);
        String value = "HashMap";

        map.put(key, value);
        System.out.println(map);

        key = null;

        System.gc();

        System.out.println(map);
    }

    private static void myWeakHashMap() {
        Map<Integer, String> map = new WeakHashMap<>();
        Integer key = new Integer(1);
        String value = "WeakHashMap";

        map.put(key, value);
        System.out.println(map);

        key = null;

        System.gc();

        System.out.println(map);
    }
}
