package com.study.dynamicprograming;

/**
 * @Author wangziyu1
 * @Date 2022/8/10 20:19
 * @Version 1.0
 * 0-1 背包问题
 */
public class KnpackProblem {
    public static void main(String[] args) {
        int W = 150;
        int[] w = {35, 30, 60, 50, 40, 10, 25};
        int[] w2 = {25, 20, 10};
        int[] v = {10, 40, 30, 50, 35, 40, 30};
        int[] v2 = {28, 20, 10};
        System.out.println(maxValue(W, w, v));
        System.out.println(maxValue(30, w2, v2));
        System.out.println(maxValue_1(30, w2, v2));
        System.out.println(maxValue2(W, w, v));
        System.out.println(maxValue2(30, w2, v2));
        System.out.println(maxValue3(30, w2, v2));
    }

    //TODO 1.「动态规划」 「背包问题」
    public static int maxValue(int capacity, int weight[], int values[]) {
        int n = weight.length;

        //定义状态 注意初始化数组的长度
        int[][] dp = new int[n + 1][capacity + 1];
        //遍历所有的子问题 ,依次计算状态
        for (int i = 1; i <= n; i++) {
            for (int j = 0; j <= capacity; j++) {
                //判断当前物品i 是否可以放的下容量为j的背包  dp[i][j] 释义就是当前容量为j的时候的物品i能放下的最大价值
                if (j >= weight[i - 1]) {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i - 1][j - weight[i - 1]] + values[i - 1]);

                } else {
                    dp[i][j] = dp[i - 1][j];
                }
            }
        }
        return dp[n][capacity];
    }

    //TODO 1.「动态规划」 「背包问题」「空间优化」 只保存一行数据
    public static int maxValue_1(int capacity, int weight[], int values[]) {
        int n = weight.length;

        //定义状态 注意初始化数组的长度
        int[] dp = new int[capacity + 1];
        //遍历所有的子问题 ,依次计算状态
        for (int i = 1; i <= n; i++) {
            for (int j = capacity; j > 0; j--) {
                //判断当前物品i 是否可以放的下容量为j的背包  dp[i][j] 释义就是当前容量为j的时候的物品i能放下的最大价值
                if (j >= weight[i - 1]) {
                    dp[j] = Math.max(dp[j], dp[j - weight[i - 1]] + values[i - 1]);
                }
            }
        }
        return dp[capacity];
    }


    //TODO 练习1.「动态规划」 「背包问题」
    public static int maxValue2(int capacity, int weight[], int values[]) {

        int n = weight.length;

        //二维数组 存放中间状态
        int[][] dp = new int[n + 1][capacity + 1];

        for (int i = 1; i <= n; i++) {
            for (int j = 0; j <= capacity; j++) {
                if (j >= weight[i - 1]) { //判断当前物品i是否能放下容量为j 的物品
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i - 1][j - weight[i - 1]] + values[i - 1]);
                } else {
                    dp[i][j] = dp[i - 1][j];
                }
            }
        }
        return dp[n][capacity];
    }


    //TODO 练习2.「动态规划」 「背包问题」
    public static int maxValue3(int capacity, int weight[], int values[]) {

        int n = weight.length;

        //存放中间状态
        int[][] dp = new int[n + 1][capacity + 1];

        for (int i = 1; i <= n; i++) {
            for (int j = 0; j <= capacity; j++) {
                if (j >= weight[i - 1]) {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i - 1][j - weight[i - 1]] + values[i - 1]);

                } else { //
                    dp[i][j] = dp[i - 1][j];
                }
            }
        }
        return dp[n][capacity];

    }
}
