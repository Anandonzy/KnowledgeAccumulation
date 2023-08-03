package com.study.strings;

import java.util.LinkedList;
import java.util.Stack;

/**
 * @Author wangziyu1
 * @Date 2023/2/24 11:13
 * @Version 1.0
 */
public class DeleteChar {

    public static void main(String[] args) {

        System.out.println(deleteCahr("aaacbbbbdefababccc"));


    }

    public static String deleteCahr(String s) {
        Stack<Character> stack = new Stack();
        //LinkedList<Character> stack2 = new LinkedList<>();
        for (int i = 0; i < s.length(); i++) {

            char c = s.charAt(i);
            if (stack.isEmpty()) {
                stack.push(c);
            } else {
                char top = stack.peek();

                // 1、c直接能消除
                // 2、ab 或者 ba 能消除
                // 3、重复执行1、2 直至不能再消除
                if((c == 'c')){
                    continue;
                }
                if ( (c == 'a' && top == 'b')
                        || (c == 'b' && top == 'a')) {
                    stack.pop();
                } else {
                    stack.push(c);
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        while (!stack.isEmpty()) {
            sb.append(stack.pop());
        }
        return sb.reverse().toString();
    }
}
