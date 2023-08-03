package com.study.strings;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @Author wangziyu1
 * @Date 2023/2/24 10:55
 * @Version 1.0
 */
public class RepeatedCharacter {

    public static void main(String[] args) {
        System.out.println(findFirstCharacter("abcaacbdefg"));
    }

    public static Character findFirstCharacter(String s) {
        Map<Character, Integer> count = new HashMap<>();
        // 统计每个字符出现的次数
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            count.put(c, count.getOrDefault(c, 0) + 1);
        }
        // 找到第一个出现完整2次的字符
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (count.get(c) == 2 && s.indexOf(c) == s.lastIndexOf(c)) {
                return c;
            }
        }
        // 没有符合条件的字符，返回空字符
        return '0';
    }
}
