package com.study.greedy;

/**
 * @Author wangziyu1
 * @Date 2022/8/10 15:46
 * @Version 1.0
 * https://leetcode.cn/problems/jump-game/
 * 给定一个非负整数数组nums ，你最初位于数组的 第一个下标 。
 * <p>
 * 数组中的每个元素代表你在该位置可以跳跃的最大长度。
 * <p>
 * 判断你是否能够到达最后一个下标。
 * 示例1：
 * <p>
 * 输入：nums = [2,3,1,1,4]
 * 输出：true
 * 解释：可以先跳 1 步，从下标 0 到达下标 1, 然后再从下标 1 跳 3 步到达最后一个下标。
 * 示例2：
 * <p>
 * 输入：nums = [3,2,1,0,4]
 * 输出：false
 * 解释：无论怎样，总会到达下标为 3 的位置。但该下标的最大跳跃长度是 0 ， 所以永远不可能到达最后一个下标。
 */
public class CanJump55 {


    public static void main(String[] args) {
        int input[] = {2, 3, 1, 1, 4};
        int input2[] = {3,2,1,0,4};
        System.out.println(canJump(input));
        System.out.println(canJump(input2));


    }

    //TODO 1. 「贪心算法」
    public static boolean canJump(int[] nums) {
        //定义一个变量 ,保存当前能跳到的最远距离
        int farthest = 0;

        for (int i = 0; i < nums.length; i++) {
            //判断当前i可以到达的范围内 更新farthest
            if (i <= farthest) {
                farthest = Math.max(farthest, i + nums[i]); //取出每次的最大值
                //如果超出末尾直接返回true
                if (farthest >= nums.length - 1) {
                    return true;
                }
            } else {
                //如果i都到不了 直接返回false
                return false;
            }
        }
        return false;
    }
}
