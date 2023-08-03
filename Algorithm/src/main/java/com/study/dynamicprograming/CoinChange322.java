package com.study.dynamicprograming;

import java.util.Arrays;

/**
 * @Author wangziyu1
 * @Date 2022/8/15 15:00
 * @Version 1.0
 * https://leetcode.cn/problems/coin-change/
 * 给你一个整数数组 coins ，表示不同面额的硬币；以及一个整数 amount ，表示总金额。
 * <p>
 * 计算并返回可以凑成总金额所需的 最少的硬币个数 。如果没有任何一种硬币组合能组成总金额，返回 -1 。
 * <p>
 * 你可以认为每种硬币的数量是无限的。
 * <p>
 *  
 * <p>
 * 示例 1：
 * <p>
 * 输入：coins = [1, 2, 5], amount = 11
 * 输出：3
 * 解释：11 = 5 + 5 + 1
 * 示例 2：
 * <p>
 * 输入：coins = [2], amount = 3
 * 输出：-1
 * 示例 3：
 * <p>
 * 输入：coins = [1], amount = 0
 * 输出：0
 */
public class CoinChange322 {

    public static void main(String[] args) {
        int input[] = {1, 2, 5};
        int amount = 11;
        System.out.println(coinChange(input, amount));
        System.out.println(coinChange2(input, amount));
        System.out.println(coinChange3(input, amount));

    }

    // TODO 1.「动态规划」
    public static int coinChange(int[] coins, int amount) {
        int[] dp = new int[amount + 1];
        dp[0] = 0;

        for (int i = 1; i <= amount; i++) {
            int minCoinNum = Integer.MAX_VALUE;
            for (int coin : coins) {
                if (coin <= i && dp[i - coin] != -1) {
                    minCoinNum = Math.min(minCoinNum, dp[i - coin] + 1);
                }
            }
            dp[i] = minCoinNum == Integer.MAX_VALUE ? -1 : minCoinNum;
        }
        return dp[amount];
    }

    // TODO 1.「动态规划」 好理解的版本
    public static int coinChange2(int[] coins, int amount) {
        int max = amount + 1;
        int[] dp = new int[amount + 1];
        Arrays.fill(dp, max);
        dp[0] = 0;

        for (int i = 1; i <= amount; i++) {
            for (int j = 0; j < coins.length; j++) {
                if (coins[j] <= i) {
                    dp[i] = Math.min(dp[i], dp[i - coins[j]] + 1);
                }
            }
        }
        return dp[amount] > amount ? -1 : dp[amount];
    }

    // TODO 练习
    public static int coinChange3(int[] coins, int amount) {

        int[] dp = new int[amount+1];
        Arrays.fill(dp, amount+1);
        dp[0] = 0;

        for (int i = 1; i <= amount; i++) {
            for (int j = 0; j < coins.length; j++) {

                if (coins[j] <= i) {
                    dp[i] = Math.min(dp[i], dp[i - coins[j]] + 1);
                }
            }
        }
        return dp[amount] > amount ? -1 : dp[amount];
    }
}
