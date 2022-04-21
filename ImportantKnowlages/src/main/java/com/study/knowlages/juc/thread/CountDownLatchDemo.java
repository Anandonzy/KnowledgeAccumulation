package com.study.knowlages.juc.thread;

import java.util.concurrent.CountDownLatch;

/**
 * @Author wangziyu1
 * @Date 2022/2/24 5:12 下午
 * @Version 1.0
 * <p>
 * CountDownLatch 可以控制线程让其阻塞完成一系列的操作之后在执行
 */
public class CountDownLatchDemo {

    /**
     * 现在有这样一个场景，假设一个自习室里有7个人，其中有一个是班长，班长的主要职责就是在其它6个同学走了后，关灯，锁教室门，然后走人，因此班长是需要最后一个走的，那么有什么方法能够控制班长这个线程是最后一个执行，而其它线程是随机执行的
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {


        /**
         * 没有使用CountDownLatch 的效果
         * 0	 is come,上完自习离开教室!
         * 1	 is come,上完自习离开教室!
         * 3	 is come,上完自习离开教室!
         * 4	 is come,上完自习离开教室!
         * 班长离开教室走人~
         * 5	 is come,上完自习离开教室!
         * 2	 is come,上完自习离开教室!
         * 会出现线程不安全的现象
         */
//        closeDoorNoCountDownLatch();
        /*    */

        /**
         * 使用CountDownLatch之后的效果
         * 0	 is come,上完自习离开教室!
         * 5	 is come,上完自习离开教室!
         * 3	 is come,上完自习离开教室!
         * 4	 is come,上完自习离开教室!
         * 2	 is come,上完自习离开教室!
         * 1	 is come,上完自习离开教室!
         * 班长离开教室走人~
         */
//        closeDoorCountDownLatch();


        /**
         * 秦灭六国.一统天下
         * 枚举类型使用
         */

        CountDownLatch countDownLatch = new CountDownLatch(6);
        for (int i = 1; i <= 6; i++) {
            new Thread(() -> {
                System.out.println(Thread.currentThread().getName() + "\t 果被灭了!");
                countDownLatch.countDown();
            }, String.valueOf(CountryEnum.forEachCountry(i).getRetMessage())).start();
        }
        countDownLatch.await();

        System.out.println("秦灭六国.一统天下~");

    }

    public static void closeDoorNoCountDownLatch() {
        for (int i = 0; i <= 5; i++) {
            new Thread(() -> {
                System.out.println(Thread.currentThread().getName() + "\t is come,上完自习离开教室!");
            }, String.valueOf(i)).start();
        }

        System.out.println("班长离开教室走人~");
    }

    public static void closeDoorCountDownLatch() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(6);
        for (int i = 0; i <= 5; i++) {
            new Thread(() -> {
                System.out.println(Thread.currentThread().getName() + "\t is come,上完自习离开教室!");
                countDownLatch.countDown();
            }, String.valueOf(i)).start();
        }
        countDownLatch.await();

        System.out.println("班长离开教室走人~");
    }
}
