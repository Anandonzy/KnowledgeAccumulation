package com.study.knowlages.test;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author wangziyu1
 * @Date 2022/3/25 11:20 上午
 * @Version 1.0
 * <p>
 * LRU算法手写测试
 */
public class LRUCacheTest {

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

        public Node() {
            this.prev = this.next = null;
        }
    }

    //构造双向链表
    class DoubleLinkedNode<K, V> {
        Node<K, V> head;
        Node<K, V> tail;

        //初始化双向链表
        public DoubleLinkedNode() {
            head = new Node<>();
            tail = new Node<>();
            head.next = tail;
            tail.prev = head;
        }

        //添加到头
        public void addHead(Node<K, V> node) {
            //从接进来的节点开始连接
            node.next = head.next;
            node.prev = head;
            head.next.prev = node;
            head.next = node;
        }


        //移除头部的节点
        public void removeHead(Node node) {
            //删除全部以node 为基准删除
            node.next.prev = node.prev;
            node.prev.next = node.next;
            node.prev = null;
            node.next = null;
        }

        //获取尾部最后一个节点
        public Node getLastNode() {
            return this.tail.prev;
        }
    }

    private int cacheSize;
    private Map<Integer, Node<Integer, Integer>> map;
    private DoubleLinkedNode<Integer, Integer> doubleLinkedNode;

    public LRUCacheTest(int cacheSize) {
        this.cacheSize = cacheSize;
        this.map = new HashMap<>();
        this.doubleLinkedNode = new DoubleLinkedNode();
    }

    //获取
    public int get(int key) {
        if(!map.containsKey(key)){
            return -1;
        }

        //获取节点
        Node<Integer, Integer> node = map.get(key);
        doubleLinkedNode.removeHead(node);
        doubleLinkedNode.addHead(node);
        return node.value;
    }

    //插入
    public void put(int key, int value) {
        if(map.containsKey(key)){ //存在则更新
            Node<Integer, Integer> node = map.get(key);
            node.value = value; //更新再放回去
            doubleLinkedNode.removeHead(node);
            doubleLinkedNode.addHead(node);
        }else{ //不存在 则插入
            if(map.size() == cacheSize){ //坑位满了.
                Node lastNode = doubleLinkedNode.getLastNode();
                map.remove(lastNode.key);// 数组移除最后一个元素
                doubleLinkedNode.removeHead(lastNode); //移除最后一个尾结点 也就是最不常用的节点.
            }
            //坑位没满 直接新增就好了
            Node<Integer, Integer> node = new Node<>(key,value);
            map.put(key, node);
            doubleLinkedNode.addHead(node); //放到链表头
        }
    }

    public static void main(String[] args) {

        LRUCacheTest lruCacheTest = new LRUCacheTest(3);
        lruCacheTest.put(1,1);
        lruCacheTest.put(2,2);
        lruCacheTest.put(3,3);
        lruCacheTest.put(4,4);
        lruCacheTest.put(1,1);
        lruCacheTest.put(7,7);
        lruCacheTest.put(1,1);
        lruCacheTest.put(1,1);
        lruCacheTest.put(1,1);
        lruCacheTest.put(3,3);
        lruCacheTest.put(2,2);
        System.out.println(lruCacheTest.map.keySet());
    }
}
