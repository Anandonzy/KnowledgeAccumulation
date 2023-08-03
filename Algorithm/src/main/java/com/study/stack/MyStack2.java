package com.study.stack;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @Author wangziyu1
 * @Date 2022/7/21 15:56
 * @Version 1.0
 */
public class MyStack2 {
    Queue<Integer> queue1;


    public MyStack2() {
        this.queue1 = new LinkedList<>();
    }


    public int pop() {
        //q1出队就是出栈
        return queue1.poll();
    }

    public int top() {
        return queue1.peek();
    }

    public boolean empty() {
        return queue1.isEmpty();
    }

    public void push(int x) {
        int l = queue1.size();
        //把x入队
        queue1.offer(x);

        //把queue的元素先出队在入队
        for (int i = 0; i < l; i++) {
            Integer value = queue1.poll();
            System.out.println(value);
            queue1.offer(value);
        }
    }

    public static void main(String[] args) {

        MyStack2 myStack = new MyStack2();
        myStack.push(1);
        myStack.push(2);
        myStack.push(3);
        System.out.println(myStack.pop());
        System.out.println(myStack.pop());
        System.out.println(myStack.pop());

    }

}
