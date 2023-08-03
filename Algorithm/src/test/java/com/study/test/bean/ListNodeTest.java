package com.study.test.bean;

/**
 * @Author wangziyu1
 * @Date 2023/2/23 15:14
 * @Version 1.0
 * 当前链表保存的数据值 和 下一个节点的引用
 */
public class ListNodeTest {
    public int val;
    public ListNodeTest next;

    public ListNodeTest() {
    }

    public ListNodeTest(int val) {
        this.val = val;
    }

    public ListNodeTest(int val, ListNodeTest next) {
        this.val = val;
        this.next = next;
    }
}
