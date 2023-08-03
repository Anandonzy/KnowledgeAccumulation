package com.study.linkedlist;

import java.util.HashMap;

import static com.study.linkedlist.TestLinkedList.printList;

/**
 * @Author wangziyu1
 * @Date 2022/7/14 10:13
 * @Version 1.0
 * 翻转链表 反转链表 一种迭代法  一种递归的方法
 */
public class ReverseLinkedList206 {

    public static void main(String[] args) {
        ListNode listNode1 = new ListNode(1);
        ListNode listNode2 = new ListNode(2);
        ListNode listNode3 = new ListNode(3);
        ListNode listNode4 = new ListNode(4);

        listNode1.next = listNode2;
        listNode2.next = listNode3;
        listNode3.next = listNode4;
        listNode4.next = null;

        printList(reverseList2(listNode1));
        //printList(listNode1);


    }


    //TODO 1.「迭代法」翻转链表
    public static ListNode reverseList(ListNode head) {
        //定义一个指针 指向当前访问的节点
        ListNode curr = head;
        ListNode prev = null;

        //依次迭代链表中的节点 ,将next指针指向prev
        while (curr != null) {
            //临时保存当前节点的下一个节点
            ListNode currNext = curr.next;
            curr.next = prev;//第一个的节点next域为null  变成最后一个
            prev = curr; //前驱等于当前节点
            curr = currNext; //当前节点 去下一个位置
        }

        //prev 指向的就是末尾的节点
        return prev;
    }


    //TODO 2.「递归」翻转列表
    public static ListNode reverseList2(ListNode head) {

        //考虑基准条件 也就是递归退出的条件
        if (head == null || head.next == null) {
            return head;
        }

        ListNode resetHead = head.next;
        ListNode newHead = reverseList2(resetHead);

        //把当前节点 接在翻转之后的节点末尾
        resetHead.next = head;
        head.next = null;
        return newHead;
    }

}
