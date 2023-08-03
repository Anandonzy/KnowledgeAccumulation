package com.study.test;

/**
 * @Author wangziyu1
 * @Date 2023/2/24 16:07
 * @Version 1.0
 * https://leetcode.cn/problems/climbing-stairs/
 */
public class ClimbStairsTest {


    public static void main(String[] args) {

        System.out.println(climbStairs2(3));
    }

    /**
     * 输入：n = 3
     * 输出：3
     * 解释：有三种方法可以爬到楼顶。
     * 1. 1 阶 + 1 阶 + 1 阶
     * 2. 1 阶 + 2 阶
     * 3. 2 阶 + 1 阶
     */

    public static int climbStairs(int n) {

        int dp[] = new int[n + 1];

        dp[0] = 1;
        dp[1] = 1;

        for (int i = 2; i <= n; i++) {
            dp[i] = dp[i - 1] + dp[i - 2];
        }
        return dp[n];

    }

    public static int climbStairs2(int n) {

        int first = 1;
        int second = 1;
        int curr = 0;
        for (int i = 2; i <= n; i++) {
            curr = first + second;
            first = second;
            second = curr;
        }
        return curr;
    }
}
