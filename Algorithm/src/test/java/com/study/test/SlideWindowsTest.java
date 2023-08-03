package com.study.test;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * @Author wangziyu1
 * @Date 2023/3/3 14:31
 * @Version 1.0
 */
public class SlideWindowsTest {


    public static void main(String[] args) {

        int[] nums = {1, 3, -1, -3, 5, 3, 6, 7};
        int[] result = slideWindowMaxValue3(nums, 3);
        for (int r : result) {
            System.out.print(r + "\t");
        }

    }

    /**
     * 「暴力解法」
     *
     * @return
     */
    public static int[] slideWindowMaxValue(int[] nums, int k) {
        int n = nums.length; //长度n
        int[] result = new int[n - k + 1]; //result数组

        for (int i = 0; i <= n - k; i++) {
            int max = nums[i];
            //找到窗口的最大值 比较大小
            for (int j = i + 1; j < i + k; j++) {
                if (nums[j] > max) {
                    max = nums[j];
                }
            }
            result[i] = max;
        }

        return result;
    }


    /**
     * 「使用堆是最好理解的」 也就是java里面的优先队列
     * 构建一个大顶堆
     *
     * @param nums
     * @param k
     * @return
     */
    public static int[] slideWindowMaxValue2(int[] nums, int k) {

        //定义大顶堆
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(k, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2 - o1;
            }
        });
        //定义一个结果数组
        int[] result = new int[nums.length - k + 1];

        for (int i = 0; i < k; i++) {
            maxHeap.add(nums[i]);
        }

        //当前大顶堆的顶堆元素就有了 就是当前窗口的最大值
        result[0] = maxHeap.peek();

        //遍历所有的窗口
        for (int i = 1; i <= nums.length - k; i++) {
            maxHeap.remove(nums[i - 1]); //删除堆中上一个窗口的元素
            maxHeap.add(nums[i + k - 1]); //添加当前窗口的最后一个元素进堆
            result[i] = maxHeap.peek();
        }
        return result;
    }

    /**
     * 线性时间复杂度 O(n)
     * 左右扫描的方式
     *
     * @param nums
     * @param k
     * @return
     */
    public static int[] slideWindowMaxValue3(int[] nums, int k) {
        int n = nums.length;

        int[] result = new int[n - k + 1];

        int[] left = new int[n];
        int[] right = new int[n];

        //从左到右扫描
        for (int i = 0; i < n; i++) {

            //从左到右
            if (i % k == 0) {
                //能整除 就是一个完整块 就是起始位置
                left[i] = nums[i];
            } else {
                //取出来前一个和当前的数据 取最大值
                left[i] = Math.max(left[i - 1], nums[i]);
            }

            //从右到左 i+j = n-1
            int j = n - 1 - i;

            if (j % k == k - 1 || j == n - 1) {
                right[j] = nums[j];
            } else {
                right[j] = Math.max(right[j + 1], nums[j]);
            }

        }

        //对每个窗口计算最大值
        for (int i = 0; i < n - k + 1; i++) {
            result[i] = Math.max(right[i], left[i + k - 1]);
        }
        return result;
    }

}
