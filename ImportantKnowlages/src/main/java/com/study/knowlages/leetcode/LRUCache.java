package com.study.knowlages.leetcode;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author wangziyu1
 * @Date 2022/3/24 4:27 下午
 * @Version 1.0
 * 手写LRU算法
 * @link https://leetcode-cn.com/problems/lru-cache/?utm_source=LCUS&utm_medium=ip_redirect&utm_campaign=transfer2china
 * <p>
 * 纯手写LRU
 * 双向链表+hash表
 */
public class LRUCache {

    //map 负责查找,构建一个虚拟的双线链表,它里面安装一个Node节点,作为数据载体 参见AQSNode实现部分
    class Node<K, V> {
        K key;
        V value;
        Node<K, V> prev;
        Node<K, V> next;

        public Node(K key, V value) {
            this.key = key;
            this.value = value;
            this.prev = this.next = null;
        }

        //空参构造
        public Node() {
            this.prev = this.next = null;
        }
    }


    //2.构建一个虚拟的双向链表
    class DoubleLinkedList<K, V> {
        Node<K, V> head;
        Node<K, V> tail;

        //初始化双向链表
        public DoubleLinkedList() {
            head = new Node<>();
            tail = new Node<>();
            head.next = tail;
            tail.prev = head;
        }

        //2.2 添加到头 画图看箭头就好
        public void addHead(Node<K, V> node) {
            node.next = head.next;
            node.prev = head;
            head.next.prev = node;
            head.next = node;
        }

        //2.3 删除节点
        /**
         * --
         *
         * @param node
         */
        public void removeNode(Node<K, V> node) {

            node.next.prev = node.prev;
            node.prev.next = node.next;
            node.prev = null;
            node.next = null;
        }

        //2.4获得最后一个节点
        public Node<K, V> getLast() {
            return tail.prev;
        }
    }

    private int cacheSize;
    private Map<Integer, Node<Integer, Integer>> map;
    private DoubleLinkedList<Integer, Integer> doubleLinkedList;

    public LRUCache(int cacheSize) {
        this.cacheSize = cacheSize;
        map = new HashMap<>();
        doubleLinkedList = new DoubleLinkedList<>();
    }

    public int get(int key) {
        if (!map.containsKey(key)) {
            return -1;
        }
        Node<Integer, Integer> node = map.get(key);
        doubleLinkedList.removeNode(node);
        doubleLinkedList.addHead(node);
        return node.value;
    }

    public void put(int key, int value) {

        if(map.containsKey(key)){//更新

            Node<Integer, Integer> node = map.get(key);
            node.value = value;
            map.put(key, node); //更新放回去
            doubleLinkedList.removeNode(node);
            doubleLinkedList.addHead(node);
        }else{
            if(map.size() == cacheSize){ //坑位满了
                Node<Integer, Integer> lastNode = doubleLinkedList.getLast();
                map.remove(lastNode.key);
                doubleLinkedList.removeNode(lastNode);
            }
            //才是新增
            Node<Integer, Integer> newNode = new Node<>(key, value);
            map.put(key, newNode);
            doubleLinkedList.addHead(newNode);
        }

    }

    public static void main(String[] args) {
        LRUCache lru = new LRUCache(2);
        lru.put(2, 2);
        lru.put(3, 3);
        lru.put(1, 1);
        lru.put(2, 2);
        lru.put(3, 3);

        System.out.println(lru.map.keySet());


    }
}

