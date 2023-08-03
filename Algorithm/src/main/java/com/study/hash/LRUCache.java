package com.study.hash;

import java.util.HashMap;

/**
 * @Author wangziyu1
 * @Date 2022/7/17 16:59
 * @Version 1.0
 * 使用「双向链表+hashmap」实现
 */
public class LRUCache {
    class Node {
        int key;
        int value;
        Node next;
        Node prev;

        public Node() {
        }

        public Node(int key, int value) {
            this.key = key;
            this.value = value;
        }
    }


    //定义属性
    private int capacity;
    private int size;
    //定义头尾指针
    Node head, tail;

    //定义一个hashmap
    private HashMap<Integer, Node> hashMap = new HashMap<Integer, Node>();

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.size = 0;
        head = new Node();
        tail = new Node();
        head.next = tail;
        tail.prev = head;
    }

    public int get(int key) {
        Node node = hashMap.get(key);
        if (node == null) { //如果不存在 则直接返回-1
            return -1;
        }
        //如果存在 将当前节点移动到队尾
        moveToTail(node);

        return node.value;
    }

    //移动到队尾
    private void moveToTail(Node node) {
        removeNode(node);
        addToTail(node);
    }

    //在链表末尾增加一个节点
    private void addToTail(Node node) {
        node.next = tail;
        node.prev = tail.prev;
        tail.prev.next = node;
        tail.prev = node;
    }

    //删除节点
    private void removeNode(Node node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    public void put(int key, int value) {
        //先根据key 查找
        Node node = hashMap.get(key);
        if (node != null) { //存在
            node.value = value;
            moveToTail(node);
        } else { //不存在
            Node newNode = new Node(key, value);
            hashMap.put(key, newNode); //保存到hash表
            addToTail(newNode); //添加到表头
            size++;
        }
        if (size > capacity) {
            Node tail = removeHead();
            hashMap.remove(tail.key);
            size --;

        }

    }

    private Node removeHead() {
        Node realHead = head.next;
        removeNode(realHead);
        return realHead;
    }

    public static void main(String[] args) {
        LRUCache lRUCache = new LRUCache(2);
        lRUCache.put(1, 1); // 缓存是 {1=1}
        lRUCache.put(2, 2); // 缓存是 {1=1, 2=2}
        System.out.println(lRUCache.get(1));    // 返回 1
        lRUCache.put(3, 3); // 该操作会使得关键字 2 作废，缓存是 {1=1, 3=3}
        System.out.println(lRUCache.get(2));    // 返回 -1 (未找到)
        lRUCache.put(4, 4); // 该操作会使得关键字 1 作废，缓存是 {4=4, 3=3}
        System.out.println(lRUCache.get(1));    // 返回 -1 (未找到)
        System.out.println(lRUCache.get(3));    // 返回 3
        System.out.println(lRUCache.get(4));    // 返回 4
    }
}
