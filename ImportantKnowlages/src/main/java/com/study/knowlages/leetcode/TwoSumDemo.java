package com.study.knowlages.leetcode;

import java.util.HashMap;

/**
 * @Author wangziyu1
 * @Date 2022/3/16 2:02 下午
 * @Version 1.0
 * <p>
 * ```Java
 * 输入：nums = [2,7,11,15], target = 9
 * 输出：[0,1]
 * 解释：因为 nums[0] + nums[1] == 9 ，返回 [0, 1] 。
 * <p>
 * ```
 * <p>
 * 示例2:
 * <p>
 * ```Java
 * 输入：nums = [3,2,4], target = 6
 * 输出：[1,2]
 * ```
 * <p>
 * 示例 3：
 * <p>
 * ```Java
 * 输入：nums = [3,3], target = 6
 * 输出：[0,1]
 * ```
 * <p>
 * 提示：
 * <p>
 * - 2 <= nums.length <= 103
 * - -109 <= nums[i] <= 109
 * - -109 <= target <= 109
 * - **只会存在一个有效答案**
 */
public class TwoSumDemo {

    /**
     * 暴力解法 时间复杂度比较高 o~2
     * @param nums
     * @param target
     * @return
     */
    public static int[] twoSum(int[] nums, int target) {

        for (int i = 0; i < nums.length; i++) {
            for (int j = i + 1; j < nums.length; j++) {
                if (target - nums[i] == nums[j]) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }

    public static int[] twoSum2(int[] nums, int target) {

        HashMap<Integer, Integer> map = new HashMap<>();

        for (int i = 0; i < nums.length; i++) {
           int partnerNumber =  target - nums[i];
           if(map.containsKey(partnerNumber)){ //判断是否含有目标值的其中之一 不存在则存到map里面  9-2=7 第一次map是没有的
               return new int[]{map.get(partnerNumber),i};
           }
            map.put(nums[i], i); //不存在 则存进去. 7 没有则存进去 (2,0)
        }
        return null;
    }

    public static void main(String[] args) {

        int[] nums = new int[]{2, 7, 11, 15};
        int target = 17;
        int[] results = twoSum2(nums, target);
        for(int r : results){
            System.out.println(r);
        }
    }

}
