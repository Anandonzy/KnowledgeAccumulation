package com.study.knowlages.juc.atomics;

import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;

/**
 * @Author wangziyu1
 * @Date 2022/4/7 16:48
 * @Version 1.0
 * 常用API
 */
public class LongAdderAPIDemo {

    public static void main(String[] args) {
        LongAdder longAdder = new LongAdder(); //只做加法
        longAdder.increment();
        longAdder.increment();
        longAdder.increment();
        System.out.println(longAdder.longValue());

        LongAccumulator longAccumulator = new LongAccumulator((x, y) -> {
            return x + y;
        }, 100); //两个操作符 以及初始值
        longAccumulator.accumulate(1);
        longAccumulator.accumulate(2);
        longAccumulator.accumulate(3);

        System.out.println(longAccumulator.longValue());
    }
}
