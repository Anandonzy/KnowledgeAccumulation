package com.study.knowlages.leetcode;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Author wangziyu1
 * @Date 2022/3/24 3:49 下午
 * @Version 1.0
 * 手写LRU算法
 * @link https://leetcode-cn.com/problems/lru-cache/?utm_source=LCUS&utm_medium=ip_redirect&utm_campaign=transfer2china
 * <p>
 * 最便捷的方法 继承LinkedHahsMap实现
 */
public class LRUdemo<K, V> extends LinkedHashMap {

    private int capacity;

    public LRUdemo(int capacity) {
        super(capacity, 0.75f, true);
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        return super.size() > capacity;
    }

    public static void main(String[] args) {

        LRUdemo<Integer, String> lru = new LRUdemo<>(2);
        lru.put(2, "b");
        lru.put(3, "c");
        lru.put(1, "a");
        System.out.println(lru.keySet());
    }
}
