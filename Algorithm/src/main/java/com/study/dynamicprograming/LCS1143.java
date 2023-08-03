package com.study.dynamicprograming;

/**
 * @Author wangziyu1
 * @Date 2022/8/12 13:00
 * @Version 1.0
 * https://leetcode.cn/problems/longest-common-subsequence/
 */
public class LCS1143 {

    public static void main(String[] args) {
        String text1 = "ezupkrb";
        String text2 = "ace";
        String text3 = "ubmrbapg";
        String text4 = "ubmrbapg";
        System.out.println(longestCommonSubsequence(text1, text2));
        System.out.println(longestCommonSubsequence(text3, text4));
        System.out.println(longestCommonSubsequence3(text1, text2));
        System.out.println(longestCommonSubsequence4(text3, text4));


    }

    // TODO 1.「动态规划」实现
    public static int longestCommonSubsequence(String text1, String text2) {

        //长度
        int l1 = text1.length();
        int l2 = text2.length();

        //定义一个二维数组 ,递推求解
        int[][] dp = new int[l1 + 1][l2 + 1];

        //遍历所有状态,递推求解
        for (int i = 1; i <= l1; i++) {
            for (int j = 1; j <= l2; j++) {
                //判断两个字符的关系 ,进行状态转移
                if (text1.charAt(i - 1) == text2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        return dp[l1][l2];
    }

    // TODO练习 2.「暴力破解」实现
    //反例 "ezupkr"
    //    "ubmrapg"
    public static int longestCommonSubsequence2(String text1, String text2) {

        int l1 = text1.length(); // ace
        int l2 = text2.length(); // abcde

        int step = 0;
        for (int i = 0; i < l1; i++) { // abcde
            for (int j = 0; j < l2; j++) { //ace
                if (text1.charAt(i) == text2.charAt(j)) {
                    step++;
                    break;
                }
            }
        }
        return step;
    }


    public static int longestCommonSubsequence3(String text1, String text2) {

        int l1 = text1.length();
        int l2 = text2.length();
        //定义dp数组 保存状态
        int[][] dp = new int[l1 + 1][l2 + 1];

        for (int i = 1; i <= l1; i++) {
            for (int j = 1; j <= l2; j++) {
                if (text1.charAt(i - 1) == text2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        return dp[l1][l2];
    }


    //TODO 练习
    public static int longestCommonSubsequence4(String text1, String text2) {

        int l1 = text1.length();
        int l2 = text2.length();

        int[][] dp = new int[l1 + 1][l2 + 1];

        for (int i = 1; i <= l1; i++) {
            for (int j = 1; j <= l2; j++) {
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
