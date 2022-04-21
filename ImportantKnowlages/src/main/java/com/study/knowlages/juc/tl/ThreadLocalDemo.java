package com.study.knowlages.juc.tl;

/**
 * @Author wangziyu1
 * @Date 2022/4/11 17:24
 * @Version 1.0
 * 1  三个售票员卖完50张票务，总量完成即可，吃大锅饭，售票员每个月固定月薪
 * <p>
 * 2  分灶吃饭，各个销售自己动手，丰衣足食
 */

/**
 * 经典买票例子 多个线程操作一个资源 每个售票口都是固定的工资
 * 公共资源只有一个笔 多个人按照顺序拿笔写字
 */
class MovieTicket {
    private int number = 50;

    /**
     * 不加锁会出现先线程安全的问题 加上锁 保证线程有序操作 不会出现线程安全问题
     * 但是性能差
     */
    public synchronized void saleTicket() {
        if (number > 0) {
            System.out.println(Thread.currentThread().getName() + "\t 卖出第" + (number--));
        } else {
            System.out.println("卖光了~");
        }
    }
}

/**
 * 体会ThreadLocal 的用法的案例
 * 实现每一个线程都有自己专属的本地变量副本(自己用自己的变量不麻烦别人，不和其他人共享，人人有份，人各一份)，
 * 主要解决了让每个线程绑定自己的值，通过使用get()和set()方法，获取默认值或将其值更改为当前线程所存的副本的值从而避免了线程安全问题。
 * <p>
 * 模拟销售提成
 * 每个人都有自己的提成 不可能每个人的工资是一样的了.
 * <p>
 * 每个人都有自己的笔 各自写各自的
 */
class House {

    /**
     * 第一种初始化ThreadLocal的方法 不推荐使用
     * 不美观
     */
    static final ThreadLocal<Integer> threadLocal = new ThreadLocal() {
        protected Integer initialValue() {
            return 0;
        }
    };
    /**
     * ThreadLocal 第二种初始化的方法 比较美观简洁 推荐使用
     * Supplier接口
     */
    static final ThreadLocal<Integer> threadLocal2 = ThreadLocal.withInitial(() -> 0);

    public void saleHouse() {
        Integer value = threadLocal2.get();
        ++value;
        threadLocal2.set(value);
    }
}


public class ThreadLocalDemo {

    public static void main(String[] args) {

        /**
         * 第一个案例的测试
         */
/*        MovieTicket movieTicket = new MovieTicket();
        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                for (int j = 0; j < 20; j++) {
                    movieTicket.saleTicket();
                }
            }, String.valueOf(i)).start();
        }*/

        /**
         * ThreadLocal的案例测试
         */
        House house = new House();
        new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    house.saleHouse();
                }
                System.out.println(Thread.currentThread().getName() + "\t" + "---卖出： " + house.threadLocal2.get());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                //必须要remove
                house.threadLocal2.remove();
            }
        }, "t1").start();

        new Thread(() -> {
            try {
                for (int i = 1; i <= 3; i++) {
                    house.saleHouse();
                }
                System.out.println(Thread.currentThread().getName() + "\t" + "---卖出： " + house.threadLocal2.get());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                house.threadLocal2.remove();
            }

        }, "t2").start();

        new Thread(() -> {
            try {
                for (int i = 1; i <= 8; i++) {
                    house.saleHouse();
                }
                System.out.println(Thread.currentThread().getName() + "\t" + "---卖出： " + house.threadLocal2.get());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                house.threadLocal2.remove();
            }
        }, "t3").start();

        System.out.println(Thread.currentThread().getName() + "\t" + "---卖出： " + house.threadLocal2.get());

    }
}
