package com.study.tree;


import java.util.ArrayList;
import java.util.LinkedList;

/**
 * @Author wangziyu1
 * @Date 2022/8/8 20:40
 * @Version 1.0
 */
public class ValidateBST98 {

    public static void main(String[] args) {
        TreeNode node5 = new TreeNode(5);
        TreeNode node1 = new TreeNode(1);
        TreeNode node4 = new TreeNode(4);
        TreeNode node3 = new TreeNode(3);
        TreeNode node6 = new TreeNode(6);

        node5.left = node1;
        node1.right = node4;
        node4.left = node3;
        node4.right = node6;

        System.out.println(isValidBST(node5));
        System.out.println(isValidBST2(node5));
        System.out.println(isValidBST3(node5));


    }

    //1. TODO「先序遍历」 根据辅助校验器判断左右值
    public static boolean isValidBST(TreeNode root) {
        if (root == null) return true;

        return validator(root, null, null);

    }

    //定义一个辅助校验器
    public static boolean validator(TreeNode root, Integer lowBound, Integer upperBound) {
        if (root == null) return true;

        //1. 判断当前节点的值是否在上下界范围内 如果超出 直接放回false
        if (lowBound != null && root.value <= lowBound) {
            return false;
        }

        if (lowBound != null && root.value >= upperBound) {
            return false;
        }
        return validator(root.left, lowBound, root.value) &&
                validator(root.right, root.value, upperBound);
    }


    //1.定义list
    public static ArrayList<Integer> inOrderArray = new ArrayList<>();

    //2. TODO「中序遍历」 利用list 「简单的方式」
    public static boolean isValidBST2(TreeNode root) {

        // 1.中序遍历
        inOrder(root);
        for (int i = 0; i < inOrderArray.size(); i++) {
            if (i > 0 && inOrderArray.get(i) <= inOrderArray.get(i - 1)) {
                return false;
            }
        }
        return true;
    }

    //中序添加到数组
    private static void inOrder(TreeNode root) {
        if (root == null) return;
        inOrder(root.left);
        inOrderArray.add(root.value);
        inOrder(root.right);
    }

    //3. TODO「使用栈中序遍历」 利用list 「简单的方式」
    public static boolean isValidBST3(TreeNode root) {
        LinkedList<TreeNode> stack = new LinkedList<>();
        double preValue = -Double.MAX_VALUE;
        //遍历访问所有节点
        while (root != null || !stack.isEmpty()) {
            while (root != null) {
                stack.push(root);
                root = root.left;
            }

            //只要栈不为空 就弹出栈顶元素,以此处理
            if (!stack.isEmpty()) {
                root = stack.pop();
                if (root.value <= preValue) {
                    return false;
                }
                preValue = root.value;
                root = root.right;
            }
        }
        return true;
    }

}
