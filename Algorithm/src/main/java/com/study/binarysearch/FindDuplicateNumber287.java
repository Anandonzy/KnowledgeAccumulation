package com.study.binarysearch;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @Author wangziyu1
 * @Date 2022/7/11 11:00
 * @Version 1.0
 * 给定一个包含 n + 1 个整数的数组 nums ，其数字都在 [1, n] 范围内（包括 1 和 n），可知至少存在一个重复的整数。
 * 假设 nums 只有 一个重复的整数 ，返回 这个重复的数 。
 * 你设计的解决方案必须 不修改 数组 nums 且只用常量级 O(1) 的额外空间。
 * 示例 1：
 * 输入：nums = [1,3,4,2,2]
 * 输出：2
 * 示例 2：
 * 输入：nums = [3,1,3,4,2]
 * 输出：3
 * 来源：力扣（LeetCode）
 * 链接：https://leetcode.cn/problems/find-the-duplicate-number
 */
public class FindDuplicateNumber287 {

    public static void main(String[] args) {


        int input[] = {1, 3, 4, 2, 2};
        System.out.println(findDuplicate6(input));
    }

    //TODO 1.使用map处理 判断有没有即可
    public static int findDuplicate(int[] nums) {
        int n = nums.length;


        HashMap<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < n; i++) {
            if (!map.containsKey(nums[i])) {
                map.put(nums[i], i);
            } else {
                return nums[i];
            }
        }
        return -1;
    }

    //TODO 2.使用map 统计出现的次数
    public static int findDuplicate2(int[] nums) {
        HashMap<Integer, Integer> map = new HashMap<>();

        for (int num : nums) {
            //没出现过
            if (map.get(num) == null) {
                map.put(num, 1);
            } else {
                return num;
            }
        }
        return -1;
    }

    //TODO 3.使用set 统计出现的次数
    public static int findDuplicate3(int[] nums) {
        Set<Integer> set = new HashSet<>();

        for (int num : nums) {
            if (set.contains(num)) {
                return num;
            } else {
                set.add(num);
            }
        }
        return -1;
    }

    //TODO 4.先排序 比较下一个跟上一个相同的
    public static int findDuplicate4(int[] nums) {
        Arrays.sort(nums);
        //遍历数组元素 遇到跟前一个相同的 就是重复元素
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] == nums[i + 1]) {
                return nums[i];
            }
        }
        return -1;
    }

    //TODO 5.二分查找
    public static int findDuplicate5(int[] nums) {

        int left = 1;
        int right = nums.length - 1;

        while (left <= right) {
            int mid = (left + right) / 2;

            //对当前的mid计算count值
            //1, 3, 4, 2, 2
            int count = 0;
            for (int j = 0; j < nums.length; j++) {
                if (nums[j] <= mid) {
                    count++;
                }
            }
            if (count <= mid) {
                left = mid + 1;
            } else {
                right = mid;
            }
            //左右指针重合的时候 找到target
            if (left == right) {
                return left;
            }
        }
        return -1;
    }

    //TODO 「最优解法」6.快慢指针 利用链表的指针 从头开始指 然后指向下一个 十分巧妙
    public static int findDuplicate6(int[] nums) {
        //1.定义快慢指针
        int fast = 0, slow = 0;

        //寻找进入点
        do {
            slow = nums[slow]; //慢指针 走一步
            fast = nums[nums[fast]]; //快指针走两步

        } while (slow != fast);

        //循环结束的时候 就是slow == fast 就是相遇点
        //2.寻找环的入口点
        int before = 0, after = slow;
        while (before != after) {
            before = nums[before];
            after = nums[after];
        }
        return before;
    }
}
