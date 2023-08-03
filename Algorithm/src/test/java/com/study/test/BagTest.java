package com.study.test;

import com.sun.imageio.plugins.common.I18N;

/**
 * @Author wangziyu1
 * @Date 2023/2/15 14:02
 * @Version 1.0
 * 0-1的背包问题
 * 动态规划的入门第一个题
 */
public class BagTest {

    public static void main(String[] args) {

        int W = 150;
        int[] w = {35, 30, 60, 50, 40, 10, 25};
        int[] w2 = {25, 20, 10};
        int[] v = {10, 40, 30, 50, 35, 40, 30};
        int[] v2 = {28, 20, 10};
        System.out.println(maxValueppp3(W, w, v));
        System.out.println(maxValueppp3(30, w2, v2));
        //System.out.println(maxValue_1(30, w2, v2));
        //System.out.println(maxValue2(W, w, v));
        //System.out.println(maxValue2(30, w2, v2));
        //System.out.println(maxValue3(30, w2, v2));
    }

    /**
     * @param capacity
     * @param weights
     * @param values
     * @return
     */
    public static int maxValueppp(int capacity, int[] weights, int[] values) {

        int n = weights.length;

        //定义状态方程 选择第一个
        int[][] dp = new int[n + 1][capacity + 1];

        //遍历所有的子问题 依次计算状态 在拿到第i个物品的时候容量为j的最大价值是多少
        for (int i = 1; i <= n; i++) { //从第一个物品开始处理
            for (int j = 0; j <= capacity; j++) {

                //判断当前物品i 是否可以放的下容量为j的背包  dp[i][j] 释义就是当前容量为j的时候的物品i能放下的最大价值
                if (j >= weights[i - 1]) {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i - 1][j - weights[i - 1]] + values[i - 1]);

                } else {
                    dp[i][j] = dp[i - 1][j];
                }
            }

        }
        return dp[n][capacity];
    }

    public static int maxValueppp2(int capacity, int[] weights, int[] values) {

        int n = weights.length;

        //定义状态方程
        int[][] dp = new int[n + 1][capacity + 1];
        for (int i = 1; i <= n; i++) {
            for (int j = 0; j <= capacity; j++) {

                //判断第i个物品要不要 比较看要的话最大价值和不要的时候的最大价值
                if (j >= weights[i - 1]) {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i - 1][j - weights[i - 1]] + values[i - 1]);
                } else {
                    dp[i][j] = dp[i - 1][j];
                }
            }
        }
        return dp[n][capacity];
    }

    /**
     * 背包问题练习 3
     *
     * @param capacity
     * @param weights
     * @param values
     * @return
     */
    public static int maxValueppp3(int capacity, int[] weights, int[] values) {

        int n = weights.length;
        int[][] dp = new int[n + 1][capacity + 1];

        for (int i = 1; i <= n; i++) {
            for (int j = 0; j <= capacity; j++) {

                if (j >= weights[i - 1]) {

                    dp[i][j] = Math.max(dp[i - 1][j], dp[i - 1][j - weights[i - 1]] + values[i - 1]);

                } else {
                    dp[i][j] = dp[i - 1][j];
                }
            }
        }
        return dp[n][capacity];
    }
}
