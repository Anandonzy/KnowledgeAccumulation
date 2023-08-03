package com.study.array;

import java.util.HashMap;

/**
 * @Author wangziyu1
 * @Date 2022/7/5 19:33
 * @Version 1.0
 * 给定一个整数数组 nums 和一个整数目标值 target，请你在该数组中找出 和为目标值 target  的那 两个 整数，并返回它们的数组下标。
 * 你可以假设每种输入只会对应一个答案。但是，数组中同一个元素在答案里不能重复出现。
 * 你可以按任意顺序返回答案。
 * <p>
 * 示例 1：
 * <p>
 * 输入：nums = [2,7,11,15], target = 9
 * 输出：[0,1]
 * 解释：因为 nums[0] + nums[1] == 9 ，返回 [0, 1] 。
 * 示例 2：
 * <p>
 * 输入：nums = [3,2,4], target = 6
 * 输出：[1,2]
 * 示例 3：
 * <p>
 * 输入：nums = [3,3], target = 6
 * 输出：[0,1]
 * <p>
 * 来源：力扣（LeetCode）
 * 链接：https://leetcode.cn/problems/two-sum
 */
public class TwoSum1 {

    public static void main(String[] args) {

        //测试方法一
        int[] input = {2, 7, 11, 15};
        //System.out.println(input.length);
        int target = 18;
        int[] results = twoSum2(input, target);
        for (int r : results) {
            System.out.println(r);
        }
    }

    //TODO 方法一 暴力法,穷举列出来所有的两数之和 时间复杂度是On平方
    public static int[] twoSum(int[] nums, int target) {
        int n = nums.length;

        for (int i = 0; i < n - 1; i++) { //第一次找一个数
            for (int j = i + 1; j < n; j++) { //第二次找下一个数字
                if (nums[i] + nums[j] == target) {
                    return new int[]{i, j};
                }
            }
        }
        throw new IllegalArgumentException("no solution");
    }

    //TODO 使用map,用targe - 每一个值 看是否有这个元素
    //时间复杂度为O(n)
    public static int[] twoSum2(int[] nums, int target) {
        HashMap<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            int result = target - nums[i]; //判断是否有这个值
            if (map.containsKey(result) && map.get(result) != i) { //存在且不是本身
                return new int[]{map.get(result), i}; //找到这个值就取出来下标和这次遍历的值
            }
            map.put(nums[i], i); //没有找到 就存放值和下标
        }
        throw new IllegalArgumentException("no solution");
    }

    //TODO「练习」

    public static int[] twoSum3(int[] nums, int target) {

        int n = nums.length;
        HashMap<Integer, Integer> map = new HashMap<>();

        for (int i = 0; i < n; i++) {
            int result = target - nums[i];
            if (map.containsKey(result) && map.get(result) != i) {
                return new int[]{map.get(result), i};
            }
            map.put(nums[i], i);
        }
        throw new IllegalArgumentException("no result¬");
    }


}
