package com.study.knowlages.juc.thread;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @Author wangziyu1
 * @Date 2022/3/28 4:23 下午
 * @Version 1.0
 * Future接口定义了操作异步任务执行一些方法，如获取异步任务的执行结果、取消任务的执行、判断任务是否被取消、判断任务执行是否完毕等。
 * Callable接口中定义了需要有返回的任务需要实现的方法。￼
 * 比如主线程让一个子线程去执行任务，子线程可能比较耗时，启动子线程开始执行任务后，
 * 主线程就去做其他事情了，过了一会才去获取子任务的执行结果。
 *
 * FutureTask 的缺点:
 *  1.阻塞 工作中不要阻塞 .高并发 尽量使用CAS 轮询自选
 *  2.建议使用 CompletableFuture
 */
public class FutureTaskDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {

        FutureTask<Integer> futureTask = new FutureTask<Integer>(() -> {

            System.out.println(Thread.currentThread().getName() + "\t future task is coming");
            try {
                TimeUnit.SECONDS.sleep(3L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 1024;
        });

        new Thread(futureTask).start();
        System.out.println("继续干活");
        while(!futureTask.isDone()){

        }
        System.out.println("获取到返回的值:" + futureTask.get());
        System.out.println("获取到返回的值:" + futureTask.get(2L,TimeUnit.SECONDS)); //如果非要使用一定使用这个 最多等2秒的时间
        /**
         * 不要用阻塞 要用轮询替代
         */
        while(true){
            if(futureTask.isDone()){
                System.out.println("获取到返回的值:" + futureTask.get());
                break;
            }else{
                System.out.println("催我也没用!!!!!");
            }

        }

    }
}
