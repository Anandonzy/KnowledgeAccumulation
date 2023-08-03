package com.study.stack;

import java.util.Stack;

/**
 * @Author wangziyu1
 * @Date 2022/7/21 17:13
 * @Version 1.0
 * * https://leetcode.cn/problems/implement-queue-using-stacks/
 */
public class MyQueue2 {


    Stack<Integer> stack1;
    Stack<Integer> stack2;

    /**
     * 用两个栈实现队列
     * 入队的时候实现翻转
     */
    public MyQueue2() {
        stack1 = new Stack<Integer>();
        stack2 = new Stack<Integer>();
    }

    public void push(int x) {
        stack1.push(x);
    }

    public int pop() {
        //1.判断stack2 是否为空,如果为空 ,就要将stack1的元素弹出 然后入栈
        if (stack2.isEmpty()) {
            while (!stack1.isEmpty()) {
                stack2.push(stack1.pop());
            }
        }
        //2.弹出stack2栈顶元素
        return stack2.pop();
    }

    public int peek() {
        //1.判断stack2 是否为空,如果为空 ,就要将stack1的元素弹出 然后入栈
        if (!stack2.isEmpty()) {
            while (!stack1.isEmpty()) {
                stack1.push(stack2.pop());
            }
        }
        //2.返回 stack2栈顶元素
        return stack2.peek();
    }

    public boolean empty() {
        return stack1.empty() && stack2.empty();
    }

    public static void main(String[] args) {

        MyQueue2 myQueue = new MyQueue2();
        myQueue.push(1);
        myQueue.push(2);
        myQueue.push(3);
        System.out.println(myQueue.pop());
        System.out.println(myQueue.pop());
        System.out.println(myQueue.pop());
    }
}
