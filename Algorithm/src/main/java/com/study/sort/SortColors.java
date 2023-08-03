package com.study.sort;

import java.util.Arrays;

/**
 * @Author wangziyu1
 * @Date 2022/8/2 18:40
 * @Version 1.0
 * <p>
 * 给定一个包含红色、白色和蓝色，一共 n 个元素的数组，原地对它们进行排序，使得相同颜色的元素相邻，并按照红色、白色、蓝色顺序排列。
 * 此题中，我们使用整数 0、 1 和 2 分别表示红色、白色和蓝色。
 * <p>
 * 进阶：
 * 	你可以不使用代码库中的排序函数来解决这道题吗？
 * 	你能想出一个仅使用常数空间的一趟扫描算法吗？
 * 示例 1：
 * 输入：nums = [2,0,2,1,1,0]
 * 输出：[0,0,1,1,2,2]
 * 示例 2：
 * 输入：nums = [2,0,1]
 * 输出：[0,1,2]
 * 示例 3：
 * 输入：nums = [0]
 * 输出：[0]
 * 示例 4：
 * 输入：nums = [1]
 * 输出：[1]
 * <p>
 * 提示：
 * 	n == nums.length
 * 	1 <= n <= 300
 * 	nums[i] 为 0、1 或 2
 */
public class SortColors {

    public static void main(String[] args) {

        int[] input = {2, 0, 2, 1, 1, 0};

        sortColors4(input);

        for (int r :
                input) {
            System.out.println(r);
        }


    }

    //TODO 1.「java原生排序」
    public static void sortColors(int[] nums) {
        Arrays.sort(nums);
    }

    //TODO 「基于选择排序」
    public static void sortColors2(int[] nums) {
        //定义一个指针 指向当前应该植入的元素的位置
        int curr = 0;

        for (int i = 0; i < nums.length; i++) {
            if (nums[i] == 0) {
                swap(nums, curr++, i);
            }
        }

        for (int i = 0; i < nums.length; i++) {
            if (nums[i] == 1) {
                swap(nums, curr++, i);
            }
        }
    }

    // 定义一个交换元素的方法
    public static void swap(int[] nums, int i, int j) {
        int temp = nums[i];
        nums[i] = nums[j];
        nums[j] = temp;
    }

    //TODO 「基于计数排序」
    public static void sortColors3(int[] nums) {

        int count0 = 0, count1 = 0, count2 = 0;

        for (int i = 0; i < nums.length; i++) {
            if (nums[i] == 0) count0++;
            else if (nums[i] == 1) count1++;
            else count2++;
        }

        for (int i = 0; i < nums.length; i++) {
            if (i < count0) {
                nums[i] = 0;
            } else if (i < count0 + count1) {
                nums[i] = 1;
            } else {
                nums[i] = 2;
            }
        }
    }

    //TODO 「基于快速排序」
    public static void sortColors4(int[] nums) {

        //定义左右指针
        int left = 0, right = nums.length - 1;

        //定义一个遍历所有元素的指针
        int i = left;

        //i= right也可以停止了.
        while (left < right && i <= right) {

            //如果是2 则换到右边 右指针左移
            while (i <= right && nums[i] == 2) {
                swap(nums, i, right--);
            }

            if(nums[i] == 0){
                swap(nums,i,left++);
            }
            i++;
        }
    }
}
