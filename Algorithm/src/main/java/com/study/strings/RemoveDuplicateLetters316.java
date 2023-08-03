package com.study.strings;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

/**
 * @Author wangziyu1
 * @Date 2022/7/12 17:13
 * @Version 1.0
 */
public class RemoveDuplicateLetters316 {

    public static void main(String[] args) {

        String input = "cbacdcbc";
        System.out.println(removeDuplicateLetters3(input));

    }


    //TODO 「暴力法」贪心算法 解决  不推荐 时间复杂度 O~3
    public static String removeDuplicateLetters(String s) {
        if (s.length() == 0) return "";
        int position = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) < s.charAt(position)) {
                boolean isReplaceable = true;
                for (int j = position; j < i; j++) {
                    boolean isDuplicated = false;
                    for (int k = i; k < s.length(); k++) {
                        if (s.charAt(k) == s.charAt(j)) {
                            isDuplicated = true;
                            break;
                        }
                    }
                    isReplaceable = isReplaceable && isDuplicated;
                }

                if (isReplaceable) {
                    position = i;
                }
            }
        }
        return s.charAt(position)
                + removeDuplicateLetters(
                s.substring(position + 1)
                        .replaceAll("" + s.charAt(position), ""));

    }

    //TODO 「贪心策略改进」使用数组保存每个字母出现的次数 然后找到最边界的字母作为起始字母 递归调用
    public static String removeDuplicateLetters2(String s) {


        if (s.length() == 0) return "";
        //希望找到最左边的字母
        int position = 0;

        //定义一个count 数组 保留所有的26个字母
        int[] count = new int[26];
        for (int i = 0; i < s.length(); i++) {
            count[s.charAt(i) - 'a']++; //数组里面有出现的次数
        }
        //遍历字母.找到当前最左端的字母
        for (int i = 0; i < s.length(); i++) {

            //判断是否是最小的
            if (s.charAt(i) < s.charAt(position)) {
                position = i;
            }

            //没遇到一个字符 ,count值都要减1
            //如果遇到count == 0 就直接退出,以当前最小的字母为最左侧的字母
            if (--count[s.charAt(i) - 'a'] == 0) break;
        }

        return s.charAt(position) +
                removeDuplicateLetters2(s.substring(position + 1).replaceAll("" + s.charAt(position), ""));
    }


    //TODO 「使用栈进行优化」 先进后出 后来的跟之前入栈的元素比较 如果有合理的(字符比他小然后后边有出现过的)
    public static String removeDuplicateLetters3(String s) {

        //定义一个栈
        Stack<Character> stack = new Stack<>();

        //为了快速判断一个字符是否在栈中出现过 我们定义一个set
        HashSet<Character> seenSet = new HashSet<Character>();

        //为了快速判断一个字符是否在某个位置之后重复出现 用一个hashMap 来保存字母出现在字符串的最后位置
        HashMap<Character, Integer> lastOccur = new HashMap<Character, Integer>();

        for (int i = 0; i < s.length(); i++) {
            lastOccur.put(s.charAt(i), i); //每一个字母最后出现的位置
        }

        //遍历字符串 判断每个字符是否需要入栈
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            // 只有c没有出现过的情况下 ,才判断是否入栈

                if (!seenSet.contains(c)) {
                //入栈之前 判断一下栈顶元素 是否在后面重复出现过 如果出现了就可以删除了.
                while (!stack.isEmpty() && c < stack.peek() && lastOccur.get(stack.peek()) > i) {
                    //满足栈顶的元素比目前的小 且 后面出现过 就删除set和栈顶的元素
                    seenSet.remove(stack.pop());
                }
                stack.push(c);
                seenSet.add(c);
            }
        }

        //遍历输出
        StringBuilder result = new StringBuilder();
        for (Character c : stack) {
            result.append(c.charValue());
        }
        return result.toString();
    }


}
