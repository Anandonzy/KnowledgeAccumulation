package com.study.test;

import com.study.linkedlist.ListNode;

/**
 * @Author wangziyu1
 * @Date 2023/2/14 12:22
 * @Version 1.0
 */
public class ReverseLinkedTest {

    public static void main(String[] args) {

        //构建一个链表
        ListNode listNode1 = new ListNode(1);
        ListNode listNode2 = new ListNode(2);
        ListNode listNode3 = new ListNode(3);
        ListNode listNode4 = new ListNode(4);

        listNode1.next = listNode2;
        listNode2.next = listNode3;
        listNode3.next = listNode4;
        listNode4.next = null;

        printList(reverseList(listNode1));
    }

    /**
     * 反转链表
     *
     * @param node
     */
    public static ListNode reverseList(ListNode node) {

        //记录当前节点
        ListNode currNode = node;

        ListNode preNode = null;

        while (currNode != null) {
            ListNode currNext = currNode.next; //使用临时节点保存当前节点的下一个
            currNode.next = preNode; //preNode 之前为空 现在变成最后一个
            preNode = currNode;
            currNode = currNext;
        }
        return preNode;
    }

    /**
     * 打印链表
     *
     * @param node
     */
    public static void printList(ListNode node) {
        ListNode curr = node;
        while (curr != null) {
            System.out.print(curr.val + "->");
            curr = curr.next;
        }
    }
}
