package com.study.hash;

import java.util.HashSet;

/**
 * @Author wangziyu1
 * @Date 2022/7/15 15:56
 * @Version 1.0
 */
public class LongestConsecutive128 {

    public static void main(String[] args) {

        int[] nums = {100, 4, 200, 1, 3, 2};
        System.out.println(longestConsecutive3(nums));


    }


    //TODO 「暴力法」 O(n^3)
    public static int longestConsecutive(int[] nums) {

        int maxLength = 0;

        //遍历数组
        for (int i = 0; i < nums.length; i++) {
            int currentNum = nums[i];

            //保存当前连续序列的长度
            int curLength = 1;

            //寻找后续数字组成连续序列
            while (contais(nums, currentNum + 1)) {
                curLength++;
                currentNum++;
            }

            //判断当前的长度是否是最大
            maxLength = curLength > maxLength ? curLength : maxLength;

        }
        return maxLength;
    }

    //定义一个方法 ,用于在数组中寻找某个元素
    public static boolean contais(int[] nums, int x) {
        for (int num : nums) {
            if (num == x) {
                return true;
            }
        }
        return false;
    }

    //TODO 「hash表」 O(n^2)
    public static int longestConsecutive2(int[] nums) {
        int maxLength = 0;

        HashSet<Integer> set = new HashSet<>();

        //遍历所有元素保存set
        for (int num : nums) {
            set.add(num);
        }


        //遍历数组
        for (int i = 0; i < nums.length; i++) {
            int currentNum = nums[i];

            //保存当前连续序列的长度
            int curLength = 1;

            //寻找后续数字组成连续序列
            while (set.contains(currentNum + 1)) {
                curLength++;
                currentNum++;
            }

            //判断当前的长度是否是最大
            maxLength = curLength > maxLength ? curLength : maxLength;
        }
        return maxLength;
    }

    //TODO 3.「hash表进一步优化」 O(n)
    public static int longestConsecutive3(int[] nums) {

        int maxLength = 0;

        HashSet<Integer> set = new HashSet<>();

        //遍历所有元素保存set
        for (int num : nums) {
            set.add(num);
        }


        //遍历数组
        for (int i = 0; i < nums.length; i++) {
            int currentNum = nums[i];

            //保存当前连续序列的长度
            int curLength = 1;

            //找前置节点 如果没有的时候 再寻找最长的长度
            if(!set.contains(currentNum-1)){
                //寻找后续数字组成连续序列
                while (set.contains(currentNum + 1)) {
                    curLength++;
                    currentNum++;
                }

                //判断当前的长度是否是最大
                maxLength = curLength > maxLength ? curLength : maxLength;
            }

        }
        return maxLength;


    }
}
