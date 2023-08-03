package com.study.leecode;

import java.util.HashMap;

/**
 * @author wangziyu
 * @version 1.0
 * @since 2023/4/14 17:25
 * https://leetcode.cn/problems/most-frequent-even-element/
 */
public class MostFrequentEven2404 {
    public static void main(String[] args) {
        System.out.println(11);

    }


    /**
     * 求出来偶数最多的数字 个数相同取最小
     *
     * @param nums
     * @return
     */
    public int mostFrequentEven(int[] nums) {
        HashMap<Integer, Integer> map = new HashMap<>();
        int maxCount = 0;
        int mostNums = -1;

        for (int curNum : nums) {
            //判断偶数
            if (curNum % 2 == 0) {
                int count = map.getOrDefault(curNum, 0) + 1;
                map.put(curNum, count); //记录出现的次数
                //统计次数跟最大的比 || 考虑相同的数字 记录数一样 取出来小的
                if (count > maxCount || (count == maxCount && curNum < mostNums)) { //比当前最大值大 记录下来
                    maxCount = count;
                    mostNums = curNum;
                }
            }
        }
        return mostNums;
    }
}
