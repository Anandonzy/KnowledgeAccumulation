package com.study.array;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @Author wangziyu1
 * @Date 2022/7/5 20:16
 * @Version 1.0
 * 给你一个包含 n 个整数的数组 nums，判断 nums 中是否存在三个元素 a，b，c ，使得 a + b + c = 0 ？请你找出所有和为 0 且不重复的三元组。
 * <p>
 * 注意：答案中不可以包含重复的三元组。
 * <p>
 *  
 * <p>
 * 示例 1：
 * <p>
 * 输入：nums = [-1,0,1,2,-1,-4]
 * 输出：[[-1,-1,2],[-1,0,1]]
 * 示例 2：
 * <p>
 * 输入：nums = []
 * 输出：[]
 * 示例 3：
 * <p>
 * 输入：nums = [0]
 * 输出：[]
 * <p>
 * 来源：力扣（LeetCode）
 * 链接：https://leetcode.cn/problems/3sum
 */
public class ThreeSum15 {

    public static void main(String[] args) {
        int[] input = new int[]{-1, 0, 1, 2, -1, -4};
        System.out.println(threeSum4(input));
    }

    //TODO 1.暴力破解 要考虑去重
    public static List<List<Integer>> threeSum(int[] nums) {
        int n = nums.length;
        //结果
        ArrayList<List<Integer>> result = new ArrayList<>();

        //三重for循环,枚举所有的三数字组合
        for (int i = 0; i < n - 2; i++) {
            for (int j = i + 1; j < n - 1; j++) {
                for (int k = j + 1; k < n; k++) {
                    if (nums[i] + nums[j] + nums[k] == 0) {
                        result.add(Arrays.asList(nums[i], nums[j], nums[k]));
                    }
                }
            }
        }
        return result;
    }

    //TODO 2.使用哈希表暴力破解优化
    public static List<List<Integer>> threeSum2(int[] nums) {
        int n = nums.length;

        //结果
        ArrayList<List<Integer>> result = new ArrayList<>();

        //定义一个hash map
        HashMap<Integer, List<Integer>> map = new HashMap<>();

        //遍历数组 , 寻找每个数对应的那个数

        for (int i = 0; i < n; i++) {
            int thatNum = 0 - nums[i];
            if (map.containsKey(thatNum)) {
                //如果找到了thatNum ,则就找到了一组解
                ArrayList<Integer> tempList = new ArrayList<>(map.get(thatNum));
                //List<Integer> tempList = map.get(thatNum);
                tempList.add(nums[i]);
                result.add(tempList);
                continue; //找到了继续
            }
            //把当前的两两组合保存到map中
            for (int j = 0; j < i; j++) {
                int newKey = nums[i] + nums[j];
                if (!map.containsKey(newKey)) {// 如果这里存在 目前没处理则也会有bug
                    ArrayList<Integer> tempList = new ArrayList<>();
                    tempList.add(nums[j]);
                    tempList.add(nums[i]);
                    map.put(newKey, tempList);
                }
            }
        }
        return result;
    }

    //TODO 3.双指针法
    public static List<List<Integer>> threeSum3(int[] nums) {

        int n = nums.length;
        ArrayList<List<Integer>> result = new ArrayList<>();
        //0. 先对数组进行排序
        Arrays.sort(nums);

        //1.遍历每一个元素,作为当前三元组中最小的那个(最矮个做核心)
        for (int i = 0; i < n; i++) {
            //1.1 如果当前数大于0 直接退出循环
            if (nums[i] > 0) {
                break;
            }
            //1.2 如果出现当前数据已经出现过 则直接跳过
            if (i > 0 && nums[i] == nums[i - 1]) {
                continue;
            }
            //1.3 常规情况 以当前数作为最小数 ,定义左右指针
            int lp = i + 1;
            int rp = n - 1;
            //只要左右指针不重叠 就继续移动指针
            while (lp < rp) {
                int sum = nums[i] + nums[lp] + nums[rp];
                //判断sum 和0 进行对比
                //1.3.1 =0的时候找到了一组解
                if (sum == 0) {
                    result.add(Arrays.asList(nums[i], nums[lp], nums[rp]));
                    lp++;
                    rp--;
                    //如果移动之后的元素相同 直接跳过
                    while (lp < rp && nums[lp] == nums[lp - 1]) lp++;
                    while (lp < rp && nums[rp] == nums[rp + 1]) rp--;

                    //1.3.2 小于0 左指针右移
                } else if (sum < 0) {
                    lp++;
                } else {
                    rp--;
                }
            }
        }
        return result;
    }

    //TODO 练习
    public static List<List<Integer>> threeSum4(int[] nums) {

        int n = nums.length;

        //结果保存
        ArrayList<List<Integer>> result = new ArrayList<>();

        //0.排序
        Arrays.sort(nums);

        //1.遍历
        for (int i = 0; i < n; i++) {

            //1.1处理特殊情况  大于0 直接跳过本次循环
            if (nums[i] > 0) {
                break;
            }

            //1.2 出现过 则跳过
            if (i > 0 && nums[i] == nums[i - 1]) {
                continue;
            }

            //定义左右指针
            int lp = i + 1;
            int rp = n - 1;
            while (lp < rp) {
                int sum = nums[i] + nums[lp] + nums[rp];
                if (sum == 0) {
                    result.add(Arrays.asList(nums[i], nums[lp], nums[rp]));
                    lp++;
                    rp--;

                    //让出现重复的则直接跳过
                    while (lp < rp && nums[lp] == nums[lp - 1]) lp++;
                    while (lp < rp && nums[rp] == nums[rp + 1]) rp--;
                } else if (sum < 0) {
                    lp++;
                } else {
                    rp--;
                }

            }
        }
        return result;
    }


}
