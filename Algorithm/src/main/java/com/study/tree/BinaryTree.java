package com.study.tree;

import java.util.LinkedList;

/**
 * @Author wangziyu1
 * @Date 2022/8/3 19:51
 * @Version 1.0
 * 二叉树的四种遍历方式
 * 	中序遍历：即左-根-右遍历，对于给定的二叉树根，寻找其左子树；对于其左子树的根，再去寻找其左子树；递归遍历，直到寻找最左边的节点i，其必然为叶子，然后遍历i的父节点，再遍历i的兄弟节点。随着递归的逐渐出栈，最终完成遍历
 * 	先序遍历：即根-左-右遍历
 * 	后序遍历：即左-右-根遍历
 * 	层序遍历：按照从上到下、从左到右的顺序，逐层遍历所有节点。
 * 1
 * 2  3
 * 4 5
 * 6
 */
public class BinaryTree {

    public static void main(String[] args) {

        /**
         *    1
         *  2  3
         *    4  5
         *      6
         */
        TreeNode node1 = new TreeNode(1);
        TreeNode node2 = new TreeNode(2);
        TreeNode node3 = new TreeNode(3);
        TreeNode node4 = new TreeNode(4);
        TreeNode node5 = new TreeNode(5);
        TreeNode node6 = new TreeNode(6);
        node1.left = node2;
        node1.right = node3;

        node3.left = node4;
        node3.right = node5;
        node4.right = node6;

        System.out.println("先序:");
        printTreePreOrder(node1);
        System.out.println();

        System.out.println("中序:");
        printTreeInOrder(node1);
        System.out.println();

        System.out.println("后序:");
        printTreePostOrder(node1);
        System.out.println();


        System.out.println("后序:");
        printTreeLevelOrder(node1);
        System.out.println();
    }


    /**
     * 1
     * 2  3
     * 4  5
     * 6
     */
    //TODO 「先序遍历」 根左右  1-> 2 -> 3 -> 4 -> 6 -> 5
    public static void printTreePreOrder(TreeNode root) {
        if (root == null) return;
        System.out.print(root.value + "\t");
        printTreePreOrder(root.left);
        printTreePreOrder(root.right);
    }

    /**
     * 1
     * 2  3
     * 4  5
     * 6
     */
    //TODO 「中序遍历」 左根右 2->1->4->6-3>->5
    public static void printTreeInOrder(TreeNode root) {
        if (root == null) return;
        printTreeInOrder(root.left);
        System.out.print(root.value + "\t");
        printTreeInOrder(root.right);
    }

    /**
     * 1
     * 2  3
     * 4  5
     * 6
     */
    //TODO 「后序遍历」 左右根  2 -> 6 ->4 -> 5 ->3 ->1
    public static void printTreePostOrder(TreeNode root) {
        if (root == null) return;
        printTreePostOrder(root.left);
        printTreePostOrder(root.right);
        System.out.print(root.value + "\t");
    }

    /**
     * 1
     * 2  3
     * 4  5
     * 6
     */
    //TODO 「层序遍历」
    public static void printTreeLevelOrder(TreeNode root) {
        LinkedList<TreeNode> list = new LinkedList<>();

        list.offer(root);

        while (!list.isEmpty()) {
            TreeNode currentNode = list.pop();
            System.out.print(currentNode.value + "\t");

            if (currentNode.left != null) {
                list.offer(currentNode.left);
            }

            if (currentNode.right != null) {
                list.offer(currentNode.right);
            }
        }
    }
}
