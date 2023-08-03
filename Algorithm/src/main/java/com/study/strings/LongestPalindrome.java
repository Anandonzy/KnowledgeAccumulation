package com.study.strings;

/**
 * @Author wangziyu1
 * @Date 2022/7/24 09:35
 * @Version 1.0
 * https://leetcode-cn.com/problems/longest-palindromic-substring/
 * 回文子串
 * <p>
 * 给定一个字符串 s，找到 s 中最长的回文子串。你可以假设 s 的最大长度为 1000。
 * 示例 1：
 * <p>
 * ```
 * 输入: "babad"
 * 输出: "bab"
 * 注意: "aba" 也是一个有效答案。
 * ```
 * 示例 2：
 * <p>
 * ```
 * 输入: "cbbd"
 * 输出: "bb"
 * ```
 */
public class LongestPalindrome {


    public static void main(String[] args) {
        System.out.println(longestPalindrome2("babad"));


    }

    public static String longestPalindrome(String s) {

        //退出判断
        if (s == null || s.length() == 0) {
            return "";
        }
        int start = 0, end = 0; //定义两个指针

        //遍历字符串
        for (int i = 0; i < s.length(); i++) {
            int len1 = expandAroundCenter(s, i, i);
            int len2 = expandAroundCenter(s, i, i + 1);
            int len = Math.max(len1, len2);
            if (len > end - start) {
                start = i - (len - 1) / 2;
                end = i + len / 2;
            }
        }
        return s.substring(start, end + 1);
    }

    private static int expandAroundCenter(String s, int left, int right) {


        //条件限制
        while (left >= 0 && right < s.length() && s.charAt(left) == s.charAt(right)) {
            --left;
            ++right;
        }

        return right - left - 1;

    }

    public static String longestPalindrome2(String s) {
        //特殊条件
        if (s == null || s.length() < 1) {
            return "";
        }
        int start = 0, end = 0;

        for (int i = 0; i < s.length(); i++) {
            int len1 = expandAroundCenter2(s, i, i);
            int len2 = expandAroundCenter2(s, i, i + 1);
            int len = Math.max(len1, len2);

            if (len > end - start) {
                start = i - (len - 1) / 2;
                end = i + len / 2;
            }
        }

        return s.substring(start, end + 1);

    }

    //从下表扩散寻找
    public static int expandAroundCenter2(String s, int left, int right) {

        while (left >= 0 && right < s.length() && s.charAt(left) == s.charAt(right)) {
            left--;
            right++;
        }

        return right - left - 1;
    }


}
