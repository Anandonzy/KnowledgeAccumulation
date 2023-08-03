package com.study.stack;

import java.util.Deque;
import java.util.LinkedList;

/**
 * @Author wangziyu1
 * @Date 2022/7/21 18:32
 * @Version 1.0
 *
 * 给定一个只包括 '('，')'，'{'，'}'，'['，']'的字符串 s ，判断字符串是否有效。
 *
 * 有效字符串需满足：
 *
 * 左括号必须用相同类型的右括号闭合。
 * 左括号必须以正确的顺序闭合。
 *
 * 示例 1：
 *
 * 输入：s = "()"
 * 输出：true
 * 示例2：
 *
 * 输入：s = "()[]{}"
 * 输出：true
 * 示例3：
 *
 * 输入：s = "(]"
 * 输出：false
 * 示例4：
 *
 * 输入：s = "([)]"
 * 输出：false
 * 示例5：
 *
 * 输入：s = "{[]}"
 * 输出：true
 *
 * 来源：力扣（LeetCode）
 * 链接：https://leetcode.cn/problems/valid-parentheses
 */
public class ValidParentheses {


    public static boolean isValid(String s) {
        //推荐使用LinkedList 当做栈
        Deque<Character> stack = new LinkedList();

        //遍历所有的字符
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);

            //如果是左括号对应的右括号入栈
            if (ch == '(') {
                stack.push(')');
            } else if (ch == '[') {
                stack.push(']');
            } else if (ch == '{') {
                stack.push('}');
            } else {

                //如果为空栈 直接返回false 如果当前的栈顶元素跟ch不匹配 也直接返回false
                if (stack.isEmpty() || stack.pop() != ch) {
                    return false;
                }
            }
        }

        return stack.isEmpty();
    }

    public static void main(String[] args) {
        System.out.println(isValid("()"));
        System.out.println(isValid("()[]{}"));
        System.out.println(isValid("(]"));
    }
}
