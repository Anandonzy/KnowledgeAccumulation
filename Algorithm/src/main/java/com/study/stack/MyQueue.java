package com.study.stack;

import java.util.Stack;

/**
 * @Author wangziyu1
 * @Date 2022/7/21 16:42
 * @Version 1.0
 * <p>
 * https://leetcode.cn/problems/implement-queue-using-stacks/
 */
public class MyQueue {

    Stack<Integer> stack1;
    Stack<Integer> stack2;

    /**
     * 用两个栈实现队列
     * 入队的时候实现翻转
     */
    public MyQueue() {
        stack1 = new Stack<Integer>();
        stack2 = new Stack<Integer>();
    }

    public void push(int x) {

        //1.将1中的数据压入 2
        while (!stack1.empty()) {
            stack2.push(stack1.pop());
        }
        //将新的元素压入stack1
        stack1.push(x);

        //2.将2 中的压入1
        while (!stack2.empty()) {
            stack1.push(stack2.pop());
        }
    }

    public int pop() {
        return stack1.pop();
    }

    public int peek() {
        return stack1.peek();
    }

    public boolean empty() {
        return stack1.empty();
    }

    public static void main(String[] args) {

        MyQueue myQueue = new MyQueue();
        myQueue.push(1);
        myQueue.push(2);
        myQueue.push(3);
        System.out.println(myQueue.pop());
        System.out.println(myQueue.pop());
        System.out.println(myQueue.pop());
    }
}
