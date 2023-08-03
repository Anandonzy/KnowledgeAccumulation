package com.study.test;

/**
 * @Author wangziyu1
 * @Date 2023/2/25 10:00
 * @Version 1.0
 * 最长公共子序列
 * abcde ace 就是公共子序列
 */
public class LongestCommonSubsequenceTest {


    public static void main(String[] args) {

        System.out.println(lcs("abcde", "ace"));
    }

    /**
     * 动态规划求解最长公共子序列
     *
     * @param text1
     * @param text2
     * @return
     */
    public static int lcs(String text1, String text2) {

        int l1 = text1.length();
        int l2 = text2.length();

        //定义动态转移返程
        int[][] dp = new int[l1 + 1][l2 + 1];

        for (int i = 1; i <= l1; i++) {
            for (int j = 1; j <= l2; j++) {
                //判断两个字符是否相等
                if (text1.charAt(i - 1) == text2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        return dp[l1][l2];
    }
}
