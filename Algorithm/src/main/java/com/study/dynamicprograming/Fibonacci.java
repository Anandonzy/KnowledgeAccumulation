package com.study.dynamicprograming;

/**
 * @Author wangziyu1
 * @Date 2022/8/10 19:37
 * @Version 1.0
 * 菲波那切数列 入门动态规划
 */
public class Fibonacci {
    public static void main(String[] args) {
        System.out.println(fib(8));
        System.out.println(fib2(8));
        System.out.println(fib3(8));
        System.out.println(fib4(8));
        System.out.println(fib5(8));
    }

    // TODO 方法一：递归
    public static int fib(int n) {
        if (n == 1 || n == 2) {
            return 1;
        }
        return fib(n - 1) + fib(n - 2);
    }

    // TODO 方法2：动态规划实现
    public static int fib2(int n) {
        if (n == 1 || n == 2) {
            return 1;
        }
        //定义一个动态数组 保存fib(n) 的计算结果
        int[] dp = new int[n];
        dp[0] = dp[1] = 1; //fib(1) 和 fib(2)

        for (int i = 2; i < n; i++) {
            dp[i] = dp[i - 2] + dp[i - 1];
        }
        return dp[n - 1];
    }

    // TODO 方法3：动态规划实现 空间优化
    public static int fib3(int n) {

        if (n == 1 || n == 2) {
            return 1;
        }
        //定义两个变量
        int pre2 = 1, pre1 = 1;
        for (int i = 2; i < n; i++) {
            int curr = pre2 + pre1;
            pre2 = pre1;
            pre1 = curr;
        }
        return pre1;
    }

    //TODO 练习.
    public static int fib4(int n) {
        if (n == 1 || n == 2) return 1;

        int dp[] = new int[n];
        dp[0] = dp[1] = 1;

        for (int i = 2; i < n; i++) {
            dp[i] = dp[i - 2] + dp[i - 1];
        }
        return dp[n - 1];
    }

    //TODO 练习.
    public static int fib5(int n) {
        if (n == 1 || n == 2) return 1;

        int pre1 =1, pre2 = 1;
        for (int i = 2; i < n; i++) {
            int curr = pre1 + pre2;
            pre2 = pre1;
            pre1 = curr;
        }
        return pre1;

    }

}
