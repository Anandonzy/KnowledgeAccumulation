package com.study.windows;

import java.util.HashMap;

/**
 * @Author wangziyu1
 * @Date 2022/7/13 19:40
 * @Version 1.0
 *
 * https://leetcode.cn/problems/minimum-window-substring/
 */
public class MinWindow76 {

    public static void main(String[] args) {

        String s = "ADOBECODEBANC";
        String t = "ABC";


        System.out.println(minWindow2(s, t));

    }

    //TODO 「暴力破解」
    public static String minWindow(String s, String t) {

        //定义结果 最小子串
        String minSubString = "";

        //定义一个hashmap 保存t中出现的字符的频次
        HashMap<Character, Integer> tCharFrequency = new HashMap<>();

        //统计字符频次
        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
            int count = tCharFrequency.getOrDefault(c, 0);
            tCharFrequency.put(c, count + 1);
        }

        //接下来s中搜索子串
        //遍历所有字符 作为当前子串的起始位置
        for (int i = 0; i < s.length(); i++) {
            //遍历i字后 不小于t长度的位置 作为子串的结束位置
            for (int j = i + t.length(); j <= s.length(); j++) {
                //统计s中子串中出现的频次
                HashMap<Character, Integer> subStrCharFrequency = new HashMap<>();

                //统计字符频次
                for (int k = i; k < j; k++) {
                    char c = s.charAt(k);
                    int count = subStrCharFrequency.getOrDefault(c, 0);
                    subStrCharFrequency.put(c, count + 1);
                }

                //如果判断的子串 符合子串的覆盖要求 并且比之前的子串短 就可以替换
                if (check(tCharFrequency, subStrCharFrequency) && (j - i < minSubString.length() || minSubString.equals(""))) {
                    minSubString = s.substring(i, j);
                }

            }
        }
        return minSubString;
    }

    //提炼一个方法 用于检查当前子串是否包含t的子串
    public static boolean check(HashMap<Character, Integer> tFreq, HashMap<Character, Integer> subStringFreq) {
        //判断t中每个字符的频次 和 subStr 进行比较
        for (char c : tFreq.keySet()) {
            if (subStringFreq.getOrDefault(c, 0) < tFreq.get(c)) {
                return false;
            }
        }
        return true;
    }

    //TODO 「滑动窗口」 特殊的滑动窗口 起始和末尾都需要移动
    public static String minWindow2(String s, String t) {
        //定义结果 最小子串
        String minSubString = "";

        //定义一个hashmap 保存t中出现的字符的频次
        HashMap<Character, Integer> tCharFrequency = new HashMap<>();

        //统计字符频次
        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
            int count = tCharFrequency.getOrDefault(c, 0);
            tCharFrequency.put(c, count + 1);
        }

        // 设置左右指针
        int lp = 0, rp = t.length();
        while ( rp <= s.length() ){
            HashMap<Character, Integer> subStrCharFrequency = new HashMap<>();
            for (int k = lp; k < rp; k++){
                char c = s.charAt(k);
                int count = subStrCharFrequency.getOrDefault(c, 0);
                subStrCharFrequency.put(c, count + 1);
            }
            if ( check(tCharFrequency, subStrCharFrequency) ) {
                if ( minSubString.equals("") || rp - lp < minSubString.length() ){
                    minSubString = s.substring(lp, rp);
                }
                lp++;
            }
            else
                rp++;
        }
        return minSubString;
    }

    //TODO 「滑动窗口优化」 特殊的滑动窗口 起始和末尾都需要移动
    public static String minWindow3(String s, String t) {

        String minSubString = "";
        HashMap<Character, Integer> tCharFrequency = new HashMap<>();
        for (int i = 0; i < t.length(); i++){
            char c = t.charAt(i);
            int count = tCharFrequency.getOrDefault(c, 0);
            tCharFrequency.put(c, count + 1);
        }
        HashMap<Character, Integer> subStrCharFrequency = new HashMap<>();
        int lp = 0, rp = 1;
        while ( rp <= s.length() ){
            char newChar = s.charAt(rp - 1);
            if ( tCharFrequency.containsKey(newChar) ){
                subStrCharFrequency.put(newChar, subStrCharFrequency.getOrDefault(newChar, 0) + 1);
            }
            while ( check(tCharFrequency, subStrCharFrequency) && lp < rp ){
                if ( minSubString.equals("") || rp - lp < minSubString.length() ){
                    minSubString = s.substring(lp, rp);
                }
                char removedChar = s.charAt(lp);
                if ( tCharFrequency.containsKey(removedChar) ){
                    subStrCharFrequency.put(removedChar, subStrCharFrequency.getOrDefault(removedChar, 0) - 1);
                }
                lp++;
            }
            rp++;
        }
        return minSubString;
    }

}
