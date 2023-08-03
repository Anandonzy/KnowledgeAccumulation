package com.study.linkedlist;

/**
 * @Author wangziyu1
 * @Date 2022/7/14 09:47
 * @Version 1.0
 * 链表的数据结构
 */
public class ListNode {

    public int val;
    public ListNode next;


    public ListNode() {
    }

    public ListNode(int val) {
        this.val = val;
    }

    public ListNode(int val, ListNode next) {
        this.val = val;
        this.next = next;
    }
}
