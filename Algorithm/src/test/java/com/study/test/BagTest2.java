package com.study.test;

/**
 * @Author wangziyu1
 * @Date 2023/2/15 14:25
 * @Version 1.0
 */
public class BagTest2 {

    public static void main(String[] args) {
        int W = 150;
        int[] w = {35, 30, 60, 50, 40, 10, 25};
        int[] w2 = {25, 20, 10};
        int[] v = {10, 40, 30, 50, 35, 40, 30};
        int[] v2 = {28, 20, 10};
        System.out.println(maxValue(W, w, v));
        System.out.println(maxValue(30, w2, v2));
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
}
