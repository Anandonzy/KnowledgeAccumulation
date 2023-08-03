package com.study.test.linkedlist;

import com.study.test.bean.ListNodeTest;

import java.util.Stack;

/**
 * @Author wangziyu1
 * @Date 2023/2/23 15:14
 * @Version 1.0
 * 删除倒数第n个节点
 */
public class ListNodeTest19 {


    public static void main(String[] args) {
        ListNodeTest listNodeTest1 = new ListNodeTest(1);
        ListNodeTest listNodeTest2 = new ListNodeTest(3);
        ListNodeTest listNodeTest3 = new ListNodeTest(4);
        ListNodeTest listNodeTest4 = new ListNodeTest(6);
        ListNodeTest listNodeTest5 = new ListNodeTest(7);

        listNodeTest1.next = listNodeTest2;
        listNodeTest2.next = listNodeTest3;
        listNodeTest3.next = listNodeTest4;
        listNodeTest4.next = listNodeTest5;
        listNodeTest5.next = listNodeTest1;
        //listNodeTest5.next = null;

        //打印测试
        //printList(listNodeTest1);

        //翻转链表
        //printList(reverseList(listNodeTest1));
        //printList(reverseList2(listNodeTest1));


        //删除倒数第2个
        //printList(removeNNode3(listNodeTest1, 2));

        //判断是否有环
        System.out.println(listIsCenter(listNodeTest1));


    }


    //TODO 「翻转链表」「迭代的方法」 需要保存上一个节点的引用
    public static ListNodeTest reverseList(ListNodeTest head) {

        //定义两个指针 一个保存当前的节点 一个保存之前的节点
        ListNodeTest curr = head;
        ListNodeTest prev = null;

        //依次迭代链表 将next 指向 prev

        while (curr != null) {
            ListNodeTest tempNode = curr.next; //当前节点的下一个保存下一个节点 为了往下移动
            curr.next = prev; //当前节点指向 prev 节点
            prev = curr; //prev也要移动
            curr = tempNode; //curr 向后移动
        }
        return prev; //prev指向的末尾的节点就是我们的新的头结点
    }


    //TODO 「递归实现翻转」
    public static ListNodeTest reverseList2(ListNodeTest head) {

        //基准情况
        if (head == null || head.next == null) {
            return head;
        }

        //一般情况 递归调用 翻转剩余的所有节点
        ListNodeTest restHead = head.next;
        ListNodeTest newHead = reverseList2(restHead);

        restHead.next = head;
        //当前节点就是链表末尾 直接指向null
        head.next = null;

        return newHead;

    }


    //TODO 「合并两个有序链表」
    public static ListNodeTest mergeTwoList(ListNodeTest l1, ListNodeTest l2) {

        //首先定义一个哨兵节点
        ListNodeTest sentinel = new ListNodeTest(-1);

        //保存当前结果链表里的最后一个节点,作为判断比较的 "前一个节点"
        ListNodeTest prev = sentinel;

        while (l1 != null && l2 != null) {
            if (l1.val <= l2.val) {
                prev.next = l1;
                prev = l1;
                l1 = l1.next;

            } else {
                prev.next = l2;
                prev = l2;
                l2 = l2.next;
            }
        }

        //循环结束 最多还有一个链表没有遍历完成 ,由于是有序的 可以直接把剩余的节点接到链表上
        prev.next = (l1 == null) ? l2 : l1;

        return sentinel.next;

    }

    //TODO 「删除链表倒数第N个节点」
    public static ListNodeTest removeNNode(ListNodeTest head, int n) {

        //获取列表的长度
        int l = getLength(head);
        //定义个哨兵节点
        ListNodeTest sentinel = new ListNodeTest(-1, head);

        ListNodeTest curr = sentinel;
        //从前到后遍历 找到正数 第l-n+1个节点
        for (int i = 0; i < l - n; i++) {
            curr = curr.next;
        }

        //循环结束就是找到了 l-n 个节点
        curr.next = curr.next.next;

        return sentinel.next;
    }

    //TODO 「删除链表倒数第N个节点」 使用「栈」
    public static ListNodeTest removeNNode2(ListNodeTest head, int n) {
        //获取列表的长度
        int l = getLength(head);
        //定义个哨兵节点
        ListNodeTest sentinel = new ListNodeTest(-1, head);

        ListNodeTest curr = sentinel;

        Stack<ListNodeTest> stack = new Stack();

        //入栈
        while (curr != null) {
            stack.push(curr);
            curr = curr.next;
        }

        //弹栈 n次
        for (int i = 0; i < n; i++) {
            stack.pop();
        }

        stack.peek().next = stack.peek().next.next;
        return sentinel.next;
    }

    //TODO 3 「删除链表倒数第N个节点」 使用「双指针的方式」 最优解法
    public static ListNodeTest removeNNode3(ListNodeTest head, int n) {

        //定义一个哨兵节点
        ListNodeTest sentinel = new ListNodeTest(-1, head);

        //定义双指针
        ListNodeTest first = sentinel, second = sentinel;


        //1.first 先走 n+1 步
        for (int i = 0; i < n + 1; i++) {
            first = first.next;
        }

        //first 和 second 同事进行 走一步就是我们要删除的

        while (first != null) {
            first = first.next;
            second = second.next;
        }
        second.next = second.next.next;
        return sentinel.next;
    }

    //判断连边是否有环
    public static boolean listIsCenter(ListNodeTest head) {
        if (head == null || head.next == null) {
            return false; //一个节点肯定无环
        }

        ListNodeTest slow = head;
        ListNodeTest fast = head.next;

        while (slow != fast) {
            if (fast == null || fast.next == null) {
                return false;
            }
            slow = slow.next;
            fast = fast.next.next; //每次移动两步
        }
        return true; //指针相遇就有环
    }


    public static int getLength(ListNodeTest head) {
        if (head == null) {
            return 0;
        }
        int length = 0;
        while (head != null) {
            length++;
            head = head.next;
        }
        return length;
    }


    public static void printList(ListNodeTest head) {

        if (head == null) {
            return;
        }
        ListNodeTest curr = head;
        while (curr != null) {
            System.out.print(curr.val + "->");
            curr = curr.next;
        }
        System.out.println("null");
    }
}
