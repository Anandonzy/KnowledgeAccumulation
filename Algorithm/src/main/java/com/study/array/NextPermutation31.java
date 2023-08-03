package com.study.array;

import java.util.Arrays;

/**
 * @Author wangziyu1
 * @Date 2022/7/7 19:47
 * @Version 1.0
 * 整数数组的一个 排列  就是将其所有成员以序列或线性顺序排列。
 * <p>
 * 例如，arr = [1,2,3] ，以下这些都可以视作 arr 的排列：[1,2,3]、[1,3,2]、[3,1,2]、[2,3,1] 。
 * 整数数组的 下一个排列 是指其整数的下一个字典序更大的排列。更正式地，如果数组的所有排列根据其字典顺序从小到大排列在一个容器中，那么数组的 下一个排列 就是在这个有序容器中排在它后面的那个排列。如果不存在下一个更大的排列，那么这个数组必须重排为字典序最小的排列（即，其元素按升序排列）。
 * <p>
 * 例如，arr = [1,2,3] 的下一个排列是 [1,3,2] 。
 * 类似地，arr = [2,3,1] 的下一个排列是 [3,1,2] 。
 * 而 arr = [3,2,1] 的下一个排列是 [1,2,3] ，因为 [3,2,1] 不存在一个字典序更大的排列。
 * 给你一个整数数组 nums ，找出 nums 的下一个排列。
 * <p>
 * 必须 原地 修改，只允许使用额外常数空间。
 * <p>
 * 示例 1：
 * <p>
 * 输入：nums = [1,2,3]
 * 输出：[1,3,2]
 * 示例 2：
 * <p>
 * 输入：nums = [3,2,1]
 * 输出：[1,2,3]
 * 示例 3：
 * <p>
 * 输入：nums = [1,1,5]
 * 输出：[1,5,1]
 * <p>
 * 来源：力扣（LeetCode）
 * 链接：https://leetcode.cn/problems/next-permutation
 */
public class NextPermutation31 {

    public static void main(String[] args) {

        int[] input = {1, 2, 3};
        nextPermutation(input);
        for (int r : input) {
            System.out.print(r + "\t");
        }
    }


    /**
     * 思路: 从后向前找到升序子序列,
     * 然后确定调整子序列的最高位,剩余部分升序排列
     *
     * @param nums
     */
    public static void nextPermutation(int[] nums) {
        int n = nums.length;
        int k = n - 2;

        //寻找最低点
        while (k >= 0 && nums[k] >= nums[k + 1]) {
            k--;
        }

        //找到k 就是需要调整的最高位

        //2,如果k=-1 说明所有的数字都是降序排列,则转换成升序排列
        if (k == -1) {
            Arrays.sort(nums);
            return;
        }

        //3.一般情况
        //3.1 依次遍历 找到需要交换的位置 (比他大的最小的那个数字)
        int i = k + 2;
        while (i < n && nums[i] > nums[k]) {
            i++;
        }

        //3.2 当前i 就是比他小的那个数字 则i-1就是要交换的那个数字
        //交换 nums[k] nums[i-1]
        int temp = nums[k];
        nums[k] = nums[i - 1];
        nums[i - 1] = temp;

        //3.3 k之后的数字转换为升序即可
        int start = k + 1;
        int end = n - 1;
        while (start < end) {
            int tmp = nums[start];
            nums[start] = nums[end];
            nums[end] = tmp;
            start++;
            end--;
        }
    }
}
