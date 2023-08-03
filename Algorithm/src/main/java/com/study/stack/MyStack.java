package com.study.stack;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @Author wangziyu1
 * @Date 2022/7/21 15:25
 * @Version 1.0
 * <p>
 * 请你仅使用两个队列实现一个后入先出（LIFO）的栈，并支持普通栈的全部四种操作（push、top、pop 和 empty）。
 * <p>
 * 实现 MyStack 类：
 * <p>
 * void push(int x) 将元素 x 压入栈顶。
 * int pop() 移除并返回栈顶元素。
 * int top() 返回栈顶元素。
 * boolean empty() 如果栈是空的，返回 true ；否则，返回 false 。
 * <p>
 * 注意：
 * <p>
 * 你只能使用队列的基本操作 —— 也就是 push to back、peek/pop from front、size 和 is empty 这些操作。
 * 你所使用的语言也许不支持队列。 你可以使用 list （列表）或者 deque（双端队列）来模拟一个队列 , 只要是标准的队列操作即可。
 * <p>
 * 来源：力扣（LeetCode）
 * 链接：https://leetcode.cn/problems/implement-stack-using-queues
 */
public class MyStack {

    Queue<Integer> queue1;
    Queue<Integer> queue2;


    /**
     * 使用两个队列实现栈的操作
     * 可以增加一个队列来做辅助。我们记原始负责存储数据的队列为queue1，新增的辅助队列为queue2。
     * 	当一个数据x压栈时，我们不是直接让它进入queue1，而是先在queue2做一个缓存。默认queue2中本没有数据，所以当前元素一定在队首。
     * queue1：a b
     * queue2：x
     * 	接下来，就让queue1执行出队操作，把之前的数据依次输出，同时全部添加到queue2中来。这样，queue2就实现了把新元素添加到队首的目的。
     * queue1：
     * queue2：x a b
     * <p>
     * 	最后，我们将queue2的内容复制给queue1做存储，然后清空queue2。在代码上，这个实现非常简单，只要交换queue1和queue2指向的内容即可。
     * queue1：x a b
     * queue2：
     */
    public MyStack() {
        queue1 = new LinkedList<Integer>();
        queue2 = new LinkedList<Integer>();
    }

    //入栈方法
    public void push(int x) {
        //queue2.add(x);
        queue2.offer(x); //添加失败 不会报错

        while (!queue1.isEmpty()) {
            queue2.offer(queue1.poll());
        }

        //交换两个队列
        Queue<Integer> temp = queue1;
        queue1 = queue2;
        queue2 = temp;
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

    public static void main(String[] args) {

        MyStack myStack = new MyStack();
        myStack.push(1);
        myStack.push(2);
        myStack.push(3);
        System.out.println(myStack.pop());
        System.out.println(myStack.pop());
        System.out.println(myStack.pop());

    }
}
