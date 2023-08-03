package com.study.hash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @Author wangziyu1
 * @Date 2022/7/14 17:36
 * @Version 1.0
 */
public class SingleNumber136 {

    public static void main(String[] args) {

        int[] input = {4, 1, 2, 1, 2};
        System.out.println(singleNumber4(input));
        System.out.println(0^4^1^2^1^2);
        System.out.println(0^4^1^1^2^2);
    }

    //TODO 1. 「暴力破解」 时间复杂度 O(n`2)
    public static int singleNumber(int[] nums) {
        //定义一个list 保存当前出现过一次的元素
        ArrayList<Integer> list = new ArrayList<>();

        //如果是int 就是移除下标 如果是Integer 就是移除元素
        for (Integer num : nums) {
            if (list.contains(num)) {
                list.remove(num);
            } else {
                list.add(num);
            }
        }
        return list.get(0);
    }

    //TODO 2.「使用map」
    public static int singleNumber2(int[] nums) {
        HashMap<Integer, Integer> map = new HashMap<>();

        for (Integer num : nums) {
            if (map.containsKey(num)) {
                map.remove(num);
            } else {
                map.put(num, 1);
            }
        }
        return map.keySet().iterator().next();
    }

    //TODO 3.「Set」[保存到set] Sum(原数组) - 2*(现数组) [1,2,2,3,3]  [原数组和11] 去重之后 [现数组1,2,3和6]
    public static int singleNumber3(int[] nums) {
        HashSet<Integer> set = new HashSet<>();
        int arrSum = 0;
        int setSum = 0;

        for (Integer num : nums) {
            set.add(num);
            arrSum += num;
        }

        for (Integer num : set) {
            setSum += num;
        }
        return 2 * setSum - arrSum;
    }

    //TODO 4.「异或运算」「数学方法」
    // a ~ 0 = a    a~a = 0
    public static int singleNumber4(int[] nums) {
        int result = 0;
        for (Integer num : nums) {
            result ^= num;
        }
        return result;
    }

}
