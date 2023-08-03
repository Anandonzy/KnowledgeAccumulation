package com.study.knowlages.juc.cf;

import java.util.concurrent.*;

/**
 * @Author wangziyu1
 * @Date 2022/3/29 19:06
 * @Version 1.0
 */
public class CompletableFutureAPIlDemo {

    public static void main(String[] args) {

        m5();
    }

    /**
     * thenCombine
     * 聚合合并多个返回值
     * 两个CompletionStage任务都完成后，最终能把两个任务的结果一起交给thenCombine 来处理
     * 先完成的先等着，等待其它分支任务
     */
    public static void m5() {
        System.out.println(CompletableFuture.supplyAsync(() -> {
            return 10;
        }).thenCombine(CompletableFuture.supplyAsync(() -> {
            return 20;
        }), (r1, r2) -> {
            return r1 + r2;
        }).thenCombine(CompletableFuture.supplyAsync(() -> {
            return 30;
        }), (r3, r4) -> {
            return r3 + r4;
        }).join());
    }

    /**
     * 对计算速度应用选用
     */
    public static void m4() {
        System.out.println(CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(3L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 1;
        }).applyToEither(CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(1L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 2;
        }), r -> {
            return r;
        }).join());
    }

    /**
     * 对计算结果进行消费
     */
    public static void m3() {
        CompletableFuture.supplyAsync(() -> {
            return 1;
        }).thenApply(f -> {
            return f + 2;
        }).thenApply(f -> {
            return f + 3;
        }).thenAccept(r -> System.out.println(r));

        //任务 A 执行完执行 B，并且 B 不需要 A 的结果
        System.out.println(CompletableFuture.supplyAsync(() -> "resultA").thenRun(() -> {
        }).join());

        //任务 A 执行完执行 B，B 需要 A 的结果，但是任务 B 无返回值
        System.out.println(CompletableFuture.supplyAsync(() -> "resultA").thenAccept(resultA -> {
        }).join());

        //任务 A 执行完执行 B，B 需要 A 的结果，同时任务 B 有返回值
        System.out.println(CompletableFuture.supplyAsync(() -> "resultA").thenApply(resultA -> resultA + " resultB").join());
    }

    /**
     * 对计算结果进行处理
     */
    public static void m2() {

        /**
         * thenApply 计算结果存在依赖关系，这两个线程串行化
         * 由于存在依赖关系(当前步错，不走下一步)，当前步骤有异常的话就叫停。
         */
        System.out.println(CompletableFuture.supplyAsync(() -> {
            System.out.println("-----1");

            return 1;
        }).thenApply(f -> {
            //由于存在依赖关系 有一步出现问题就直接叫停.
            //int age = 100 / 0;
            System.out.println("-----2");

            return f + 1;
        }).thenApply(f -> {
            System.out.println("-----3");

            return f + 2;
        }).whenComplete((v, e) -> {
            if (e == null) {
                System.out.println("result ----" + v);
            }
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        }).join());


        System.out.println();
        /**
         * handle 方法
         * 有异常也可以走下一步 跟
         */
        System.out.println(CompletableFuture.supplyAsync(() -> {
            System.out.println("-----1");
            return 1;
        }).handle((f, e) -> {
            System.out.println("-----2");
            //int age = 100 / 0;
            return f + 1;
        }).handle((f, e) -> {
            System.out.println("-----3");
            return f + 2;
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        }).join());
    }

    /**
     * 获得结果和触发计算
     */
    public static void m1() throws ExecutionException, InterruptedException, TimeoutException {
        //获得结果和触发计算
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 10, 1L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(50), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());

        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(1L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 1;
        }, threadPoolExecutor);


        //阻塞获取结果
        //System.out.println(future.get());

        //设定时间 获取 ,时间到了没获取到 就会TimeoutException
        //System.out.println(future.get(2L, TimeUnit.SECONDS));

        try {
            TimeUnit.SECONDS.sleep(3L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //算完了给我结果 没算完给我默认值
        System.out.println(future.getNow(999));

        //complete打断上述计算操作 如果打断成功 则用自己的默认值 -11 如果打断失败,则用获取到的计算值.
        //System.out.println(future.complete(-11) + "\t" + future.get());

        threadPoolExecutor.shutdown();
    }

}
