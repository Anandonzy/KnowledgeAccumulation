package com.study.linkedlist;

import static com.study.linkedlist.TestLinkedList.printList;

/**
 * @Author wangziyu1
 * @Date 2022/7/14 14:30
 * @Version 1.0
 * 将两个升序链表合并为一个新的 升序 链表并返回。新链表是通过拼接给定的两个链表的所有节点组成的。
 * <p>
 * <p>
 * 示例 1：
 * 输入：l1 = [1,2,4], l2 = [1,3,4]
 * 输出：[1,1,2,3,4,4]
 * 示例 2：
 * <p>
 * 输入：l1 = [], l2 = []
 * 输出：[]
 * 示例 3：
 * <p>
 * 输入：l1 = [], l2 = [0]
 * 输出：[0]
 * <p>
 * 提示：
 * <p>
 * 两个链表的节点数目范围是 [0, 50]
 * -100 <= Node.val <= 100
 * l1 和 l2 均按 非递减顺序 排列
 * <p>
 * 来源：力扣（LeetCode）
 * 链接：https://leetcode.cn/problems/merge-two-sorted-lists
 */
public class MergeTwoSortedLists21 {

    public static void main(String[] args) {
        ListNode listNode1 = new ListNode(1);
        ListNode listNode2 = new ListNode(2);
        ListNode listNode3 = new ListNode(3);
        ListNode listNode4 = new ListNode(4);

        listNode1.next = listNode2;
        listNode2.next = listNode3;
        listNode3.next = listNode4;
        listNode4.next = null;

        ListNode listNode5 = new ListNode(5);
        ListNode listNode6 = new ListNode(6);
        ListNode listNode7 = new ListNode(7);
        ListNode listNode8 = new ListNode(8);

        listNode5.next = listNode6;
        listNode6.next = listNode7;
        listNode7.next = listNode8;
        listNode8.next = null;

        printList(mergeTwoLists2(listNode1, listNode5));

    }


    //TODO 「迭代」
    public static ListNode mergeTwoLists(ListNode list1, ListNode list2) {
        //1.首先定义一个哨兵节点
        ListNode sentinel = new ListNode(-1);

        //保存当前结果链表的最后一个节点 作为判断比较的前一个节点
        ListNode prev = sentinel;


        //迭代两个链表 至少有一个为null
        while (list1 != null && list2 != null) {
            if (list1.val <= list2.val) {
                prev.next = list1; //将list1 节点链接到prev后面
                prev = list1;  //指针向前移 以下一个节点作为链表的头结点
                list1 = list1.next;
            } else {
                prev.next = list2;
                prev = list2;
                list2 = list2.next;
            }
        }
        //两个链表不知道那个长 最多还有一个链表没遍历完成 因为已经排序了 所以可以直接把剩余节点接大到链表上
        prev.next = (list1 == null) ? list2 : list1;

        return sentinel.next;
    }

    //TODO 「递归」
    public static ListNode mergeTwoLists2(ListNode list1, ListNode list2) {
        if (list1 == null) return list2;
        if (list2 == null) return list1;

        if (list1.val <= list2.val) {
            list1.next = mergeTwoLists2(list1.next, list2);
            return list1;
        } else {
            list2.next = mergeTwoLists2(list2, list2.next);
            return list2;
        }
    }

}
