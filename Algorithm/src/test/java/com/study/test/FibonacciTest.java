package com.study.test;

/**
 * @Author wangziyu1
 * @Date 2023/2/15 13:35
 * @Version 1.0
 * 斐波那契数列引入 动态规划
 * 使用一个数组保存中间的状态
 */
public class FibonacciTest {


    public static void main(String[] args) {
        System.out.println(fib(9));
        System.out.println(fib2(9));


    }

    public static int fib(int n) {
        if (n == 1 || n == 2) {
            return 1;
        }

        //定义一个状态数组 保存fib(n)计算的结果
        int[] dp = new int[n];
        dp[0] = dp[1] = 1; //fib(1) 和fib(2)
        for (int i = 2; i < n; i++) {
            dp[i] = dp[i - 2] + dp[i - 1];

        }
        return dp[n - 1];
    }

    //在优化 空间没必要全部保存中间的状态
    public static int fib2(int n) {
        if(n==1 || n==2){
            return 1;
        }

        //定义两个临时变量 保存两个状态
        int pre2 = 1, pre1 = 1;

        for (int i = 2; i < n; i++) {
            int curr = pre2 + pre1;
            pre2 = pre1;
            pre1 = curr;
        }
        return pre1;
    }
}
