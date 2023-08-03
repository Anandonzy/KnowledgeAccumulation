package com.study.hash;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author wangziyu1
 * @Date 2022/7/19 19:18
 * @Version 1.0
 */
public class LRUCache2 {

    //定义HashMap
    Map<Integer, Node<Integer, Integer>> map;

    //定义 容量
    int capacity;

    //定义双向队列
    DoublueLinkedNode doublueLinkedNode;

    public LRUCache2(int capacity) {
        this.capacity = capacity;
        this.map = new HashMap<Integer, Node<Integer, Integer>>();
        doublueLinkedNode = new DoublueLinkedNode();
    }

    class Node<K, V> {
        K key;
        V values;
        //定义头尾指针
        Node next;
        Node prev;

        public Node(K key, V values) {
            this.key = key;
            this.values = values;
            this.next = this.prev = null;
        }

        public Node() {
        }
    }

    //定义双向链表
    class DoublueLinkedNode<K, V> {

        Node<K, V> head;
        Node<K, V> tail;

        public DoublueLinkedNode() {
            head = new Node<K, V>();
            tail = new Node<K, V>();
            head.next = tail;
            tail.prev = head;
        }

        public Node getLast() {
            return this.tail.prev;
        }

        public void removeNode(Node node) {
            node.next.prev = node.prev;
            node.prev.next = node.next;
            node.next = null;
            node.prev = null;
        }

        public void addHead(Node node) {
            node.next = head.next; //1
            node.prev = head; //2
            node.next.prev = node; //3
            head.next = node; //4
        }
    }


    public void put(int key, int value) {
        if (!map.containsKey(key)) { //不存在 就插入数据
            if (capacity == map.size()) { //判断坑位满没满
                //删除最后一个节点
                Node lastNode = doublueLinkedNode.getLast();
                map.remove(lastNode.key);
                doublueLinkedNode.removeNode(lastNode);
            }
            Node<Integer, Integer> newNode = new Node<>(key, value);
            map.put(key, newNode);
            doublueLinkedNode.addHead(newNode);
        } else { //更新
            Node<Integer, Integer> oldNode = map.get(key);
            oldNode.values = value;
            map.put(key, oldNode); //更新数据
            doublueLinkedNode.removeNode(oldNode);
            doublueLinkedNode.addHead(oldNode);
        }
    }

    public int get(int key) {
        //获取数据
        Node<Integer, Integer> node = map.get(key);
        //移除节点
        doublueLinkedNode.removeNode(node);
        doublueLinkedNode.addHead(node);
        return node.values;
    }

    public static void main(String[] args) {

        LRUCache2 lru = new LRUCache2(2);
        lru.put(2, 2);
        lru.put(3, 3);
        lru.put(1, 1);
        lru.put(2, 2);
        lru.put(3, 3);
        lru.get(2);
        lru.put(1, 1);

        System.out.println(lru.map.keySet());
    }
}
