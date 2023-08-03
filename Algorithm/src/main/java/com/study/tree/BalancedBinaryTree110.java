package com.study.tree;

/**
 * @Author wangziyu1
 * @Date 2022/8/8 20:06
 * @Version 1.0
 */
public class BalancedBinaryTree110 {

    public static void main(String[] args) {
        TreeNode node3 = new com.study.tree.TreeNode(3);
        TreeNode node9 = new com.study.tree.TreeNode(9);
        TreeNode node20 = new com.study.tree.TreeNode(20);
        TreeNode node15 = new com.study.tree.TreeNode(15);
        TreeNode node7 = new com.study.tree.TreeNode(7);
        node3.left = node9;
        node3.right = node20;
        node20.left = node15;
        node20.right = node7;
        System.out.println(isBalanced(node3));
        System.out.println(isBalanced2(node3));


    }

    //TODO 1.「先序遍历」 求树的高度 从顶向下
    public static boolean isBalanced(TreeNode root) {
        if (root == null) return true;

        //1.判断左右子树的高度差

        return Math.abs(height(root.left) - height(root.right)) <= 1
                && isBalanced(root.left)
                && isBalanced(root.right);
    }

    //定义一个计算树的方法
    public static int height(TreeNode root) {
        if (root == null) return 0;
        return Math.max(height(root.left), height(root.right)) + 1;
    }


    //TODO 2.「后序遍历」 判断树是否平衡
    public static boolean isBalanced2(TreeNode root) {
        if (root == null) {
            return true;
        }
        return balanceHeight(root) > -1;
    }

    //定义一个直接判断返回当前是否是平衡的方法,也返回高度.
    public static int balanceHeight(TreeNode root) {
        if (root == null) return 0;
        int leftHeight = balanceHeight(root.left);
        int rightHeight = balanceHeight(root.right);

        //判断是否平衡
        if (leftHeight == -1 || rightHeight == -1 || Math.abs(leftHeight - rightHeight) > 1) {
            return -1;
        }
        //如果平衡 返回当前高度
        return Math.max(leftHeight, rightHeight) + 1;
    }
}
