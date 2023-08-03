package com.study.tree;


/**
 * @Author wangziyu1
 * @Date 2022/7/24 09:22
 * @Version 1.0
 * 翻转二叉树
 * https://leetcode-cn.com/problems/invert-binary-tree/
 * 翻转一棵二叉树。
 * 示例：
 * 输入：
 * ```
 * 4
 * /   \
 * 2     7
 * / \   / \
 * 1   3 6   9
 * ```
 * 输出：
 * <p>
 * ```
 * 4
 * /   \
 * 7     2
 * / \   / \
 * 9   6 3   1
 * ```
 */
public class InvertTree226 {

    public static void main(String[] args) {

        /**
         *      4
         *    /   \
         *   2     7
         *  / \   / \
         * 1   3 6   9
         */
        com.study.tree.TreeNode node1 = new com.study.tree.TreeNode(1);
        com.study.tree.TreeNode node2 = new com.study.tree.TreeNode(2);
        com.study.tree.TreeNode node3 = new com.study.tree.TreeNode(3);
        com.study.tree.TreeNode node4 = new com.study.tree.TreeNode(4);
        com.study.tree.TreeNode node6 = new com.study.tree.TreeNode(6);
        com.study.tree.TreeNode node7 = new com.study.tree.TreeNode(7);
        com.study.tree.TreeNode node9 = new com.study.tree.TreeNode(9);

        node4.left = node2;
        node4.right = node7;
        node2.left = node1;
        node2.right = node3;
        node7.left = node6;
        node7.right = node9;
        BinaryTree binaryTree = new BinaryTree();
        binaryTree.printTreeLevelOrder(node4);

        System.out.println();
        //invertTree(node4);
        invertTree2(node4);
        binaryTree.printTreeLevelOrder(node4);


    }

    //TODO 「先序遍历」 翻转 先处理根 翻转 然后再翻转左右节点
    public static TreeNode invertTree(TreeNode root) {
        if (root == null) return null;

        //1. 先处理根节点
        TreeNode temp = root.left;
        root.left = root.right;
        root.right = temp;

        //2. 递归处理左右节点
        invertTree(root.left);
        invertTree(root.right);
        return root;
    }

    //TODO 「后序遍历」 先翻转左右节点 在处理根节点
    public static TreeNode invertTree2(TreeNode root) {
        if(root ==null) return null;

        //1.先递归处理 左右节点
        TreeNode left = invertTree2(root.left);
        TreeNode right = invertTree2(root.right);


        //2.再处理 根节点
        root.left = right;
        root.right = left;

        return root;
    }
}
