package com.study.dynamicprograming;

/**
 * @Author wangziyu1
 * @Date 2022/8/12 11:33
 * @Version 1.0
 * https://leetcode.cn/problems/climbing-stairs/
 * 这是一道非常经典的面试题。爬楼梯需要一步一步爬，很明显，这可以划分阶段、用动态规划的方法来解决。
 * 对于爬n级台阶，考虑它的最后一步，总是有两种情况可以到达：从n-1级台阶处，爬1个台阶；或者从n-2级台阶处，爬2个台阶。
 * 所以我们可以将到达n级台阶的方法保存下来，记为f(n)。
 * <p>
 * 于是有状态转移方程：
 * f(n)=f(n-1)+f(n-2)
 */
public class ClimbingStairs {

    public static void main(String[] args) {
        System.out.println(climbStairs1(3));
        System.out.println(climbStairs2(3));
        System.out.println(climbStairs3(3));


    }

    // TODO 1. 「动态规划」
    public static int climbStairs1(int n) {

        //定义保存中间状态
        int[] dp = new int[n + 1];
        dp[0] = 1;
        dp[1] = 1;

        for (int i = 2; i <= n; i++) {
            dp[i] = dp[i - 2] + dp[i - 1];
        }
        return dp[n];
    }

    // TODO 2. 「动态规划」 空间优化
    public static int climbStairs2(int n) {

        int pre1 = 1;
        int pre2 = 1;
        int curr;

        for (int i = 2; i <= n; i++) {
            curr = pre1 + pre2;
            pre2 = pre1;
            pre1 = curr;
        }
        return pre1;
    }


    // TODO 3. 「数学方法」 菲波那切数列通用解决方案
    public static int climbStairs3(int n) {

        double squrt_5 = Math.sqrt(5);
        double fib = (Math.pow((1 + squrt_5) / 2, n + 1) - Math.pow((1 - squrt_5) / 2, n + 1))/squrt_5;
        return (int) fib;
    }
}
