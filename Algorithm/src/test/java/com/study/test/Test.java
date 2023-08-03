package com.study.test;

import java.util.ArrayDeque;

/**
 * @Author wangziyu1
 * @Date 2023/2/14 15:38
 * @Version 1.0
 */
public class Test {


    public static void main(String[] args) {
        // 1 3 2

        int[] num = {1, 3, 4, -1, 2};
        int res = getMaxSum(num, 3);
        System.out.println(res);


    }


    /**
     * @param nums 传入的数组
     * @param n    个长度的和
     * @return 1, 3,-1,-3,5,3,5,7
     */
    public static int[] maxSliedWindow(int[] nums, int n) {

        //1.定义一个结果数组
        int[] result = new int[nums.length - n + 1];

        //定义一个双端队列
        ArrayDeque<Integer> deque = new ArrayDeque<>();

        //初始化双端队列
        for (int i = 0; i < n; i++) {

            //如果是队尾比他小 就直接删除
            while (!deque.isEmpty() && nums[i] > nums[deque.getLast()]) {
                deque.removeLast();
            }
            deque.addLast(i);
        }

        //第一个窗口的最大值
        result[0] = nums[deque.getFirst()];

        //遍历所有数组
        for (int i = n; i < nums.length; i++) {
            //判断如果上一个窗口删除的 就是窗口的最大值 那么需要将队列中的最大值删掉
            if (!deque.isEmpty() && deque.getFirst() == i - n) {
                deque.removeFirst();
            }

            //判断下一个新增的元素是否可以删除 队尾元素
            //如果队尾元素比他还小 直接删
            while (!deque.isEmpty() && nums[i] > nums[deque.getLast()]) {
                deque.removeLast();
            }

            //保存元素
            deque.addLast(i);
            result[i - n + 1] = nums[deque.getFirst()];
        }
        return result;
    }

    //TODO 「双端队列」 两头控制 ArrayDeque
    public static int[] maxSlidingWindow3(int[] nums, int k) {

        //定义结果数组
        int[] result = new int[nums.length - k + 1];

        //定义双端队列
        ArrayDeque<Integer> deque = new ArrayDeque<>();

        //初始化双端队列 保存元素的索引 1, 3, -1, -3, 5, 3, 6, 7
        for (int i = 0; i < k; i++) {

            //如果队尾元素别他小 就直接删除
            while (!deque.isEmpty() && nums[i] > nums[deque.getLast()]) {
                deque.removeLast();
            }
            deque.addLast(i);
        }

        //第一个窗口的最大值
        result[0] = nums[deque.getFirst()];

        //遍历数组所有的元素
        for (int i = k; i < nums.length; i++) {
            //判断如果上一个窗口删掉的就是窗口最大值 那么需要将队列中的最大值删掉
            if (!deque.isEmpty() && deque.getFirst() == i - k) {
                deque.removeFirst();
            }

            //判断新增元素是否可以删除队尾元素
            //如果队尾元素别他小 就直接删除
            while (!deque.isEmpty() && nums[i] > nums[deque.getLast()]) {
                deque.removeLast();
            }
            deque.addLast(i);

            //保存元素
            result[i - k + 1] = nums[deque.getFirst()];

        }

        return result;
    }


    public static int getMaxSum(int[] arr, int n) {
        int maxSum = 0;
        int sum = 0;
        for (int i = 0; i < n; i++) {
            sum += arr[i];
        }
        maxSum = sum;
        for (int i = n; i < arr.length; i++) {
            sum = sum + arr[i] - arr[i - n];
            maxSum = Math.max(sum, maxSum);
        }
        return maxSum;
    }
}
