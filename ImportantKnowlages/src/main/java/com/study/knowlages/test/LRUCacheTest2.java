package com.study.knowlages.test;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author wangziyu1
 * @Date 2022/3/25 3:42 下午
 * @Version 1.0
 */
public class LRUCacheTest2 {
    private int cacheSize;
    private Map<Integer, Node<Integer, Integer>> map;
    private DoubleLikedNode doubleLinedNode;

    public LRUCacheTest2(int cacheSize) {
        this.cacheSize = cacheSize;
        map = new HashMap<>();
        doubleLinedNode = new DoubleLikedNode();
    }

    //节点要有key ,value ,和 前指针 和 后指针.
    class Node<K, V> {
        K key;
        V value;
        Node<K, V> next;
        Node<K, V> prev;

        public Node(K key, V value) {
            this.key = key;
            this.value = value;
            this.prev = this.next = null;
        }

        public Node() {
            this.prev = this.next = null;
        }
    }

    class DoubleLikedNode<K, V> {
        Node<K, V> head;
        Node<K, V> tail;

        //初始化双向链表
        public DoubleLikedNode() {
            this.head = new Node<>();
            this.tail = new Node<>();
            this.head.next = tail;
            this.tail.prev = head;
        }

        //添加节点
        public void addHead(Node<K,V> node) {
            node.next = head.next;
            node.prev = head;
            head.next.prev = node;
            head.next = node;
        }

        public void removeNode(Node<K,V> node) {
            node.next.prev = node.prev;
            node.prev.next = node.next;
            node.prev = null;
            node.next = null;
        }

        public Node<K, V> lastNode() {
            return tail.prev;
        }
    }

    public void put(int key, int value) {
        if(map.containsKey(key)){ //更新
            Node<Integer, Integer> node = map.get(key);
            node.value = value;
            map.put(key, node);
            doubleLinedNode.removeNode(node);
            doubleLinedNode.addHead(node);
        }else{
            if(map.size() == cacheSize){ //坑位满了 移除最后一个
                Node lastNode = doubleLinedNode.lastNode();
                map.remove(lastNode.key);
                doubleLinedNode.removeNode(lastNode);
            }
            //没满 则直接加入 然后在加入
            Node<Integer, Integer> node = new Node<>(key, value);
            map.put(key, node);
            doubleLinedNode.addHead(node);
        }
    }

    public int get(int key) {
        if (!map.containsKey(key)) {
            return -1;
        }
        //存在的时候返回
        Node<Integer, Integer> node = map.get(key);
        doubleLinedNode.removeNode(node);
        return node.value;
    }

    public static void main(String[] args) {

        LRUCacheTest2 lru = new LRUCacheTest2(3);
        lru.put(2, 2);
        lru.put(3, 3);
        lru.put(1, 1);
        lru.put(2, 2);
        lru.put(3, 3);
        lru.put(2, 2);
        lru.put(4, 4);

        System.out.println(lru.map.keySet());

    }
}
