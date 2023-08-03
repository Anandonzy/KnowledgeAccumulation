package com.study.test;

import com.study.test.bean.TreeNodeTest;
import com.study.tree.TreeNode;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * @Author wangziyu1
 * @Date 2023/2/14 10:09
 * @Version 1.0
 */
public class BinaryTreeTest {

    public static void main(String[] args) {


        /**
         *    1
         *  2  3
         *    4  5
         *      6
         */
        TreeNodeTest node1 = new TreeNodeTest(1);
        TreeNodeTest node2 = new TreeNodeTest(2);
        TreeNodeTest node3 = new TreeNodeTest(3);
        TreeNodeTest node4 = new TreeNodeTest(4);
        TreeNodeTest node5 = new TreeNodeTest(5);
        TreeNodeTest node6 = new TreeNodeTest(6);

        node1.left = node2;
        node1.right = node3;

        node3.left = node4;
        node3.right = node5;
        node4.right = node6;

        LinkedList<Integer> list = new LinkedList<>();
        list.offer(1);
        list.offer(2);
        list.offer(3);
        list.offer(4);
        list.offer(5);

        System.out.println(list.pop());

        //中序遍历
        //printTreeInOrder(node1);

        //先序遍历
        //printTreePreOrder(node1);

        //后续遍历
        //printTreePostOrder(node1);

        //层序遍历
        //printTreeLevelOrder(node1);
    }

    /**
     * 层序遍历
     * 1 2 3 4 5 6
     *
     * @param root
     */
    private static void printTreeLevelOrder(TreeNodeTest root) {

        if (root == null) {
            return;
        }
        LinkedList<TreeNodeTest> list = new LinkedList<>();

        list.offer(root);

        while (!list.isEmpty()) {
            TreeNodeTest currentTree = list.pop();

            System.out.println(currentTree.value + "\t");

            if (currentTree.left != null) {
                list.offer(currentTree.left);
            }

            if (currentTree.right != null) {
                list.offer(currentTree.right);
            }

        }


    }

    /**
     * 后续遍历
     * 左右根
     *
     * @param root
     */
    private static void printTreePostOrder(TreeNodeTest root) {
        if (root == null) {
            return;
        }
        printTreePostOrder(root.left);
        printTreePostOrder(root.right);
        System.out.println(root.value);
    }

    /**
     * 根左右
     *
     * @param root
     */
    private static void printTreePreOrder(TreeNodeTest root) {

        if (root == null) {
            return;
        }
        System.out.println(root.value);
        printTreePreOrder(root.left);
        printTreePreOrder(root.right);
    }

    /**
     * 中序遍历 左根右
     *
     * @param root
     */
    private static void printTreeInOrder(TreeNodeTest root) {
        if (root == null) {
            return;
        }
        printTreeInOrder(root.left);
        System.out.println(root.value);
        printTreeInOrder(root.right);
    }


}
