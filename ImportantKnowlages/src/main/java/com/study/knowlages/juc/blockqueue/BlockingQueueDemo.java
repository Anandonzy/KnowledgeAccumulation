package com.study.knowlages.juc.blockqueue;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * @Author wangziyu1
 * @Date 2022/2/25 4:34 下午
 * @Version 1.0
 *
 * 1.队列
 * 先进先出
 * 2.阻塞队列
 * 2.1 阻塞队列有没有好的一面
 *
 * 2.2 不得不阻塞,你如何管理
 */
public class BlockingQueueDemo {


    public static void main(String[] args) throws InterruptedException {
    // 你用过List集合类
        List list = null;

    // ArrayList集合类熟悉么？

    // 还用过 CopyOnWriteList  和 BlockingQueue

        /**
         * 抛出异常组\
         * 但执行add方法，向已经满的ArrayBlockingQueue中添加元素时候，会抛出异常
         */
        ArrayBlockingQueue<String> arrayBlockingQueue = new ArrayBlockingQueue<>(3);
        System.out.println(arrayBlockingQueue.add("a"));
        System.out.println(arrayBlockingQueue.add("b"));
        System.out.println(arrayBlockingQueue.add("c"));
//        System.out.println(arrayBlockingQueue.add("3"));
//        System.out.println(arrayBlockingQueue.remove("a"));
//        System.out.println(arrayBlockingQueue.remove("b"));
//        System.out.println(arrayBlockingQueue.remove("c"));
//        System.out.println(arrayBlockingQueue.remove());
        System.out.println(arrayBlockingQueue.element());

        System.out.println("-----------------------------");


        /**
         * 布尔类型组
         */

        BlockingQueue blockingQueue = new ArrayBlockingQueue(3);

        System.out.println(blockingQueue.offer("a"));
        System.out.println(blockingQueue.offer("b"));
        System.out.println(blockingQueue.offer("c"));
        System.out.println(blockingQueue.offer("d"));

        System.out.println(blockingQueue.poll());
        System.out.println(blockingQueue.poll());
        System.out.println(blockingQueue.poll());
        System.out.println(blockingQueue.poll());

        System.out.println("-----------------------------");

        /**
         * 阻塞队列组
         */
        BlockingQueue<String> blockingQueue2 = new ArrayBlockingQueue<>(3);
        blockingQueue2.put("a");
        blockingQueue2.put("b");
        blockingQueue2.put("c");
//        System.out.println("================");

       blockingQueue2.take();
       blockingQueue2.take();
       blockingQueue2.take();
//       blockingQueue2.take();

        System.out.println("-----------------------------");

        /**
         *不见不散组
         * offer( ) ， poll 加时间
         *
         * 使用offer插入的时候，需要指定时间，如果2秒还没有插入，那么就放弃插入
         */
        BlockingQueue<String> blockingQueu3 = new ArrayBlockingQueue<>(3);
        System.out.println("----");
        System.out.println(blockingQueu3.offer("a", 2L, TimeUnit.SECONDS));
        System.out.println(blockingQueu3.offer("b", 2L, TimeUnit.SECONDS));
        System.out.println(blockingQueu3.offer("c", 2L, TimeUnit.SECONDS));
        System.out.println(blockingQueu3.offer("d", 2L, TimeUnit.SECONDS));
        System.out.println("----");


        /**
         * 测试SynchronousQueue添加元素的过程
         *
         * 首先我们创建了两个线程，一个线程用于生产，一个线程用于消费
         *
         * 生产的线程分别put了 A、B、C这三个字段
         */
        BlockingQueue<String> blockingQueu4 = new SynchronousQueue<>();
        new Thread(() -> {
            try {
                System.out.println(Thread.currentThread().getName() + "\t put A ");
                blockingQueu4.put("A");

                System.out.println(Thread.currentThread().getName() + "\t put B ");
                blockingQueu4.put("B");

                System.out.println(Thread.currentThread().getName() + "\t put C ");
                blockingQueu4.put("C");

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "t1").start();

//        消费线程使用take，消费阻塞队列中的内容，并且每次消费前，都等待5秒

        new Thread(() -> {
            try {

                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                blockingQueu4.take();
                System.out.println(Thread.currentThread().getName() + "\t take A ");

                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                blockingQueu4.take();
                System.out.println(Thread.currentThread().getName() + "\t take B ");

                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                blockingQueu4.take();
                System.out.println(Thread.currentThread().getName() + "\t take C ");

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "t2").start();

    }
}
