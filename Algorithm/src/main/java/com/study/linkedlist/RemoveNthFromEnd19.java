package com.study.linkedlist;


import java.util.List;
import java.util.Stack;

import static com.study.linkedlist.TestLinkedList.printList;

/**
 * @Author wangziyu1
 * @Date 2022/7/14 15:19
 * @Version 1.0
 * https://leetcode.cn/problems/remove-nth-node-from-end-of-list/
 */
public class RemoveNthFromEnd19 {

    public static void main(String[] args) {
        ListNode listNode1 = new ListNode(1);
        ListNode listNode2 = new ListNode(2);
        ListNode listNode3 = new ListNode(3);
        ListNode listNode4 = new ListNode(4);

        listNode1.next = listNode2;
        listNode2.next = listNode3;
        listNode3.next = listNode4;
        listNode4.next = null;

        printList(removeNthFromEnd3(listNode1, 1));

    }


    //TODO 1.「计算长度法」 倒数第n个则就是 链表长度 l-n+1个
    public static ListNode removeNthFromEnd(ListNode head, int n) {

        //1.遍历链表长度
        int l = getLength(head);

        //定义一个哨兵节点
        ListNode sentinel = new ListNode(-1, head);

        ListNode curr = sentinel;

        //遍历链表
        for (int i = 0; i < l - n; i++) {
            curr = curr.next; //找到我们要删除的节点
        }

        //删除要找的这个节点
        curr.next = curr.next.next;

        return sentinel.next;
    }

    /**
     * 返回链表的长度
     *
     * @param head
     * @return
     */
    public static int getLength(ListNode head) {
        int count = 0;
        while (head != null) {
            count++;
            head = head.next;
        }
        return count;
    }


    //TODO 2.「栈」 找到元素弹栈
    public static ListNode removeNthFromEnd2(ListNode head, int n) {

        //哨兵节点
        ListNode sentinel = new ListNode(-1, head);
        ListNode curr = sentinel;

        //定义栈
        Stack<ListNode> stack = new Stack<>();

        //全部入栈
        while (curr != null) {
            stack.push(curr);
            curr = curr.next;
        }

        //弹栈n次
        for (int i = 0; i < n; i++) {
            stack.pop();
        }
        //删除要删除的元素
        stack.peek().next = stack.peek().next.next;

        return sentinel.next;
    }
    //TODO 3.「双指针」「遍历一遍法」 最优解 时间复杂度最低

    /**
     * 保持固定距离 start / end 之间的距离就是n 当start 到达尾部的时候 end指向的就是我们要找的
     *
     * @param head
     * @param n
     * @return
     */
    public static ListNode removeNthFromEnd3(ListNode head, int n) {

        //定义哨兵节点
        ListNode sentinel = new ListNode(-1, head);


        //定义双指针
        ListNode first = sentinel, second = sentinel;

        //找到第n个 其实是让first走 n+1 步
        for (int i = 0; i < n + 1; i++) {
            first = first.next;
        }

        //first 和 second 一起移动
        while (first != null) {
            first = first.next;
            second = second.next;
        }
        //等到结束的时候就是 要删除的倒数第n个元素
        second.next = second.next.next;
        return sentinel.next;
    }

}
