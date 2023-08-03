package com.study.hash;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author wangziyu1
 * @Date 2022/7/19 10:03
 * @Version 1.0
 * LRU 哈希表+双向链表实现
 */
public class LRUCache1 {

    //定义容量
    int capacity;

    //使用hashmap进行检索
    Map<Integer, Node<Integer, Integer>> map;

    //创建双向链表
    DoubleLinkedNode<Integer, Integer> doubleLinkedNode;

    //定义Node作为数据载体
    class Node<K, V> {
        K key;
        V value;
        Node<K, V> next;
        Node<K, V> prev;

        public Node(K key, V value) {
            this.key = key;
            this.value = value;
            this.next = this.prev = null;
        }

        public Node() {
            this.next = this.prev = null;
        }
    }

    //构建双向链表
    class DoubleLinkedNode<K, V> {
        Node<K, V> head;
        Node<K, V> tail;

        public DoubleLinkedNode() {
            head = new Node<>();
            tail = new Node<>();
            head.next = tail;
            tail.prev = head;
        }


        //添加到队头
        public void addHead(Node node) {
            node.next = head.next; // 1
            node.prev = head; // 2
            node.next.prev = node; //3
            head.next = node;
        }

        //删除节点
        public void removeNode(Node node) {
            node.next.prev = node.prev;
            node.prev.next = node.next;
            node.next = null;
            node.prev = null;
        }

        public Node getTailNode() {
            return this.tail.prev;
        }
    }

    //获取节点
    public int get(int key) {
        if (map.containsKey(key)) {
            Node<Integer, Integer> node = map.get(key);
            doubleLinkedNode.removeNode(node);
            doubleLinkedNode.addHead(node);
            return node.value;
        } else {
            return -1;
        }
    }

    //添加
    public void put(int key, int value) {
        if (!map.containsKey(key)) { //添加

            if (map.size() == capacity) { //坑位满了
                Node lastNode = doubleLinkedNode.getTailNode();
                map.remove(lastNode.key);
                doubleLinkedNode.removeNode(lastNode);
            }

            Node<Integer, Integer> newNode = new Node<>(key, value);
            map.put(key, newNode);
            doubleLinkedNode.addHead(newNode);
        } else { //更新
            Node<Integer, Integer> oldNode = map.get(key);
            oldNode.value = value;
            map.put(key, oldNode);
            doubleLinkedNode.removeNode(oldNode);
            doubleLinkedNode.addHead(oldNode);
        }
    }

    public LRUCache1(int capacity) {
        this.capacity = capacity;
        map = new HashMap<Integer, Node<Integer, Integer>>();
        doubleLinkedNode = new DoubleLinkedNode<Integer, Integer>();
    }

    public static void main(String[] args) {

        LRUCache1 lru = new LRUCache1(2);
        lru.put(2, 2);
        lru.put(3, 3);
        lru.put(1, 1);
        lru.put(2, 2);
        lru.put(3, 3);
        System.out.println(lru.map.keySet());

    }
}
