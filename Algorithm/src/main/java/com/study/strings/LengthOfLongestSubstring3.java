package com.study.strings;

import java.util.HashMap;
import java.util.HashSet;

/**
 * @Author wangziyu1
 * @Date 2022/7/20 19:13
 * @Version 1.0
 * <p>
 * https://leetcode.cn/problems/longest-substring-without-repeating-characters/
 */
public class LengthOfLongestSubstring3 {


    public static int lengthOfLongestSubstring(String s) {
        if (s.length() == 0) return 0;
        HashSet<Character> occSet = new HashSet<>();
        int n = s.length();

        //定义两个指针 右指针，初始值为 -1，相当于我们在字符串的左边界的左侧，还没有开始移动
        int left = -1, max = 0;

        //遍历
        for (int i = 0; i < n; i++) {
            if (i != 0) { //第一次不用删除  开始滑动
                //左指针移动一个字符,删除一个
                occSet.remove(s.charAt(i - 1));
            }
            //右指针开始移动
            while (left + 1 < n && !occSet.contains(s.charAt(left + 1))) {
                occSet.add(s.charAt(left + 1));
                ++left;
            }
            max = Math.max(max, left - i + 1);
        }
        /**
         *   a   w   a   s   d
         *       l
         */
        return max;

    }


    public static void main(String[] args) {
        String input = "awasda";
        System.out.println(lengthOfLongestSubstring(input));
    }
}
