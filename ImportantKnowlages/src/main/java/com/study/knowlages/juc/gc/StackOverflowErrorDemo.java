package com.study.knowlages.juc.gc;

/**
 * @Author wangziyu1
 * @Date 2022/3/8 11:01 上午
 * @Version 1.0 栈溢出Demo
 */
public class StackOverflowErrorDemo {

    public static void main(String[] args) {
        stackOverflowError();
    }
    /**
     * 栈一般是512K，不断的深度调用，直到栈被撑破
     * Exception in thread "main" java.lang.StackOverflowError
     */
    private static void stackOverflowError() {
        stackOverflowError();
    }
}
