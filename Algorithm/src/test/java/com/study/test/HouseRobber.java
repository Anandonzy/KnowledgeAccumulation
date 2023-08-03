package com.study.test;

/**
 * @Author wangziyu1
 * @Date 2023/2/25 10:47
 * @Version 1.0
 * 打家劫舍 不能偷连续的
 */
public class HouseRobber {

    public static void main(String[] args) {

        int[] num = {1, 2, 3, 1};
        System.out.println(houseRobber(num));

    }

    public static int houseRobber(int[] nums) {

        int length = nums.length;
        //从倒数第n-1 和 n-2个就要考虑偷不偷

        int[] dp = new int[length + 1];
        dp[0] = 0;
        dp[1] = nums[0];

        for (int i = 2; i <= length; i++) {
            dp[i] = Math.max(dp[i - 1], dp[i - 2] + nums[i - 1]);
        }
        return dp[length];
    }
}
