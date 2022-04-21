package com.study.knowlages.juc.thread;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @Author wangziyu1
 * @Date 2022/2/22 2:30 下午
 * @Version 1.0 证明Arraylist 不安全
 * 线程不安全的问题
 * 因为arraylist里面的写操作没有加锁
 */
public class ContainerNotSafeDemo {

    public static void main(String[] args) {


       /* List<String> list = Arrays.asList("a", "b", "c");

        list.forEach(System.out::println);*/

//        List<String> list = new ArrayList<>();
//        List<String> list = new Vector<String>();
//        List<String> list = Collections.synchronizedList(new ArrayList<>());
        CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();

//        list.add("a");
//        list.add("b");
//        list.add("c");

        for (String element : list) {
            System.out.println(element);
        }

        //起三个线程
        for (int i = 1; i <= 30; i++) {
            new Thread(() -> {

                list.add(UUID.randomUUID().toString().substring(0, 8));
                System.out.println(list);
            }, String.valueOf(i)).start();

        }

        /**
         * 1.故障现象:
         *  java.util.ConcurrentModificationException
         *
         * 2.导致原因
         *  add方法里面没加锁
         *  并发争抢修改导致
         *
         * 3.解决方案
         *  1.Vector
         *  2.Collections.synchronizedList(new ArrayList());
         *  3.juc里面的 CopyOnWriteArrayList
         *  4.
         * 4.优化建议(同样的错误如何不在犯)
         *
         */

        new CopyOnWriteArraySet<>();
        new HashSet();


            Map<String, String> map = new HashMap<>();

        /**
         * 也是 ConcurrentModificationException
         */
        for (int i = 0; i < 30; i++) {
                new Thread(() -> {
                    map.put(Thread.currentThread().getName(), UUID.randomUUID().toString().substring(0, 8));
                    System.out.println(map);
                }, String.valueOf(i)).start();
            }




    }
}
