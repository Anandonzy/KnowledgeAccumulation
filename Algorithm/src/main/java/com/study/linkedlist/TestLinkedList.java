package com.study.linkedlist;

import java.util.HashMap;
import java.util.List;

/**
 * @Author wangziyu1
 * @Date 2022/7/14 09:48
 * @Version 1.0
 */
public class TestLinkedList {

    public static void main(String[] args) {

        //构建一个链表 把所有的节点连接起来
        ListNode listNode1 = new ListNode(2);
        ListNode listNode2 = new ListNode(3);
        ListNode listNode3 = new ListNode(5);
        ListNode listNode4 = new ListNode(17);

        listNode1.next = listNode2; // 2->3
        listNode2 = listNode3;  // 3
        listNode3.next = listNode4;
        listNode4.next = null;
        printList(listNode1);
        HashMap<Object, Object> map = new HashMap<>();

        map.put(1, 1);

    }

    public static void printList(ListNode head) {
        ListNode curNode = head;
        while (curNode != null) {
            System.out.print(curNode.val + "->");
            curNode = curNode.next;
        }
        System.out.print("null");

    }
}
