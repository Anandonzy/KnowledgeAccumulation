package com.study.dynamicprograming;

/**
 * @Author wangziyu1
 * @Date 2022/8/15 13:19
 * @Version 1.0
 * <p>
 * 由于不能偷窃连续的房屋，我们自然想到，隔一个偷一间显然是一个不错的选择。那是不是，直接计算所有奇数项的和，以及所有偶数项的和，取最大值就可以了呢？并没有这么简单。
 * 例如，如果是[2，7，1，3，9]，很明显，偷2，1，9或者7，3都不是最佳选择，偷7，9才是。
 * 这里的关键是，对于三个连续的房屋2，7，1，由于跟后面的9都隔开了，所以我们可以选择偷2，1，也可以直接选择偷7。这就需要分情况讨论了。
 * 所以我们发现，从最后往前倒推，最后一间屋n，有偷和不偷两种选择：
 * 	如果偷，那么前一间屋n-1一定没有偷，我们考虑n-2之前的最优选择，加上n就可以了；
 * 	如果不偷，那么n-1之前的最优选择，就是当前的最优选择。
 * <p>
 * 所以，这明显是一个动态规划的问题。
 * <p>
 * https://leetcode.cn/problems/house-robber/
 * <p>
 * 动态方程:
 * dp[i] = Math.max(dp[i-1],dp[i-2]+num[i-1])
 */
public class HouseRobber198 {

    public static void main(String[] args) {

        int input[] = new int[]{1, 2, 3, 1};
        int input2[] = new int[]{2, 7, 9, 3, 1};
        System.out.println(rob(input2));
        System.out.println(rob2(input2));

    }

    //TODO 1.「动态规划」 求解
    public static int rob(int[] nums) {

        int n = nums.length;

        //定义动态转移数组
        int[] dp = new int[n + 1];
        dp[0] = 0;
        dp[1] = nums[0];

        for (int i = 2; i <= n; i++) {
            dp[i] = Math.max(dp[i - 1], dp[i - 2] + nums[i - 1]);
        }

        return dp[n];
    }

    //TODO 2.「动态规划」空间优化 求解
    public static int rob2(int[] nums) {
        if (nums == null || nums.length == 0)
            return 0;
        int n = nums.length;

        int pre1 = nums[0];
        int pre2 = 0;
        for (int i = 1; i < n; i++) {
            int sum = Math.max(pre1, pre2 + nums[i]);
            pre2 = pre1;
            pre1 = sum;
        }

        return pre1;
    }


}
