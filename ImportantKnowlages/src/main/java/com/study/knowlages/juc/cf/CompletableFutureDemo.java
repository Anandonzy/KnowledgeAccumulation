package com.study.knowlages.juc.cf;

import java.util.concurrent.*;

/**
 * @Author wangziyu1
 * @Date 2022/3/28 5:22 下午
 * @Version 1.0
 * FutureTask的优化版本
 * CompletableFuture 的简单使用
 */
public class CompletableFutureDemo {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //自定义的线程池.
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 20, 1L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(50), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());


        //简单使用
        //CompletableFutureHelloWorld();

        //函数式编程 ComletableFuture
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(3L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return ThreadLocalRandom.current().nextInt(10);
        }).thenApply(f -> {
            if(f>10){ //修改这里可以看到异常的情况
                return f / 0;
            }
            return f + 2;
        }).whenComplete((v, e) -> {
            if (e == null) {
                System.out.println("result is :" + v);
            }
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });

        System.out.println(Thread.currentThread().getName() + "\t main is over");

        //主线程不要立刻结束，否则CompletableFuture默认使用的线程池会立刻关闭:暂停3秒钟线程
    /*    try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        future.join();
        threadPoolExecutor.shutdown();
    }

    /**
     * ### CompletableFuture的优点
     * <p>
     * 1. 异步任务结束时，会自动回调某个对象的方法；
     * 2. 异步任务出错时，会自动回调某个对象的方法；
     * 3. 主线程设置好回调后，不再关心异步任务的执行，异步任务之间可以顺序执行
     *
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static void CompletableFutureHelloWorld() throws InterruptedException, ExecutionException {
        //自定义的线程池.
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 20, 1L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(50), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());


        //什么都不传使用默认的线程池
        CompletableFuture future1 = CompletableFuture.runAsync(() -> {
            System.out.println(Thread.currentThread().getName() + "\t come in");
        });

        //可以使用自己的线程池
        CompletableFuture future2 = CompletableFuture.runAsync(() -> {
            System.out.println(Thread.currentThread().getName() + "\t come in");
        }, threadPoolExecutor);

        System.out.println(future1.get());
        System.out.println(future2.get());

        //什么都不传使用默认的线程池
        CompletableFuture future3 = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName() + "\t come in");
            return 11;
        });

        //可以使用自己的线程池
        CompletableFuture future4 = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName() + "\t come in");
            return 22;
        }, threadPoolExecutor);

        System.out.println(future3.get());
        System.out.println(future4.get());


        threadPoolExecutor.shutdown();
    }
}
