package com.study.windows;


import java.util.ArrayList;
import java.util.List;

/**
 * @Author wangziyu1
 * @Date 2022/7/13 20:38
 * @Version 1.0
 * <p>
 * 给定两个字符串s和 p，找到s中所有p的异位词的子串，返回这些子串的起始索引。不考虑答案输出的顺序。
 * <p>
 * 异位词 指由相同字母重排列形成的字符串（包括相同的字符串）。
 * <p>
 * <p>
 * 示例1:
 * <p>
 * 输入: s = "cbaebabacd", p = "abc"
 * 输出: [0,6]
 * 解释:
 * 起始索引等于 0 的子串是 "cba", 它是 "abc" 的异位词。
 * 起始索引等于 6 的子串是 "bac", 它是 "abc" 的异位词。
 * 示例 2:
 * <p>
 * 输入: s = "abab", p = "ab"
 * 输出: [0,1,2]
 * 解释:
 * 起始索引等于 0 的子串是 "ab", 它是 "ab" 的异位词。
 * 起始索引等于 1 的子串是 "ba", 它是 "ab" 的异位词。
 * 起始索引等于 2 的子串是 "ab", 它是 "ab" 的异位词。
 * <p>
 * 提示:
 * <p>
 * 1 <= s.length, p.length <= 3 * 104
 * s和p仅包含小写字母
 * <p>
 * <p>
 * 来源：力扣（LeetCode）
 * 链接：https://leetcode.cn/problems/find-all-anagrams-in-a-string
 */
public class FindAnagrams438 {

    public static void main(String[] args) {

        String s = "cbaebabacd";
        String p = "abc";

        List<Integer> result = findAnagrams2(s, p);

        for (int i : result) {
            System.out.println(i);
        }


    }

    //TODO 「暴力解法」
    public static List<Integer> findAnagrams(String s, String p) {
        ArrayList<Integer> result = new ArrayList<>();

        //1.统计p的字母出现的频次
        int[] pCharCounts = new int[26];
        for (int i = 0; i < p.length(); i++) { //每一个字母出现的频次
            pCharCounts[p.charAt(i) - 'a']++;
        }

        //遍历 s 以每一个字母作为起始  考虑长度为p.length 的子串
        for (int i = 0; i <= s.length() - p.length(); i++) {
            //判断当前子串是否为p的字母异位词
            boolean isMatch = true;

            //定义一个数组 统计子串中的字母频次
            int[] subStrCharCounts = new int[26];
            for (int j = i; j < i + p.length(); j++) { //每一个字母出现的频次
                subStrCharCounts[s.charAt(j) - 'a']++;

                //判断当前字符频次.如果超过p中的频次 就一定不符合要求
                if (subStrCharCounts[s.charAt(j) - 'a'] > pCharCounts[s.charAt(j) - 'a']) {
                    isMatch = false;
                    break;
                }
            }

            if (isMatch) {
                result.add(i);
            }

        }
        return result;
    }


    //TODO 2「滑动窗口（双指针）」
    public static List<Integer> findAnagrams2(String s, String p) {
        int[] pCharCounts = new int[26];
        for (int i = 0; i < p.length(); i++) {
            pCharCounts[p.charAt(i) - 'a']++;
        }


        //1. 定义双指针 指向窗口起始和结束的位置
        int start = 0, end = 1;
        ArrayList<Integer> result = new ArrayList<>();
        // 定义左右指针
        int lp = 0, rp = 1;
        int[] subStrCharCounts = new int[26];

        //2.移动指针 总是截取字符出现频次全部小于p的字母的频次的子串
        while (end <= s.length()) {
            char newchar = s.charAt(end - 1);
            subStrCharCounts[newchar - 'a']++;
            //3.判断当前子串是否符合要求
            while (subStrCharCounts[newchar - 'a'] > pCharCounts[newchar - 'a'] && start < end) {
                char removeChar = s.charAt(start);
                subStrCharCounts[removeChar - 'a']--;
                start++;
            }

            //如果当前子串长度 等于p的长度 那么就是一个字母异位词
            if (end - start == p.length()) {
                result.add(start);
            }
            end++;
        }

        return result;
    }
}
