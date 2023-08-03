package com.study.sort;

/**
 * @Author wangziyu1
 * @Date 2022/7/30 16:47
 * @Version 1.0
 * 冒泡排序也是一种简单的排序算法。
 * 它的基本原理是：重复地扫描要排序的数列，一次比较两个元素，如果它们的大小顺序错误，就把它们交换过来。这样，一次扫描结束，我们可以确保最大（小）的值被移动到序列末尾。
 * 这个算法的名字由来，就是因为越小的元素会经由交换，慢慢“浮”到数列的顶端。
 */
public class BubbleSort {

    public static void bubbleSort(int[] a) {
        int length = a.length;
        int temp;
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a.length - i - 1; j++) {
                if (a[j] > a[j + 1]) {
                    temp = a[j];
                    a[j] = a[j + 1];
                    a[j + 1] = temp;
                }
            }
        }
    }


    public static void main(String[] args) {
        int[] nums = {3, 2, 1, 5, 6};
        bubbleSort2(nums);
        for (int num : nums) {
            System.out.print(num + " ");
        }
    }

    /**
     * 面试前模拟练习
     */
    public static void bubbleSort2(int[] nums) {
        int n = nums.length;

        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (nums[j] > nums[j + 1]) {
                    int temp = nums[j];
                    nums[j] = nums[j + 1];
                    nums[j + 1] = temp;
                }
            }
        }
    }
}
