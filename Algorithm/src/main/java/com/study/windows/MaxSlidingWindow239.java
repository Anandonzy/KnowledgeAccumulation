package com.study.windows;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * @Author wangziyu1
 * @Date 2022/7/13 11:19
 * @Version 1.0
 * <p>
 * 给你一个整数数组 nums，有一个大小为k的滑动窗口从数组的最左侧移动到数组的最右侧。你只可以看到在滑动窗口内的 k个数字。滑动窗口每次只向右移动一位。
 * <p>
 * 返回 滑动窗口中的最大值 。
 * <p>
 * <p>
 * 示例 1：
 * <p>
 * 输入：nums = [1,3,-1,-3,5,3,6,7], k = 3
 * 输出：[3,3,5,5,6,7]
 * 解释：
 * 滑动窗口的位置                最大值
 * ---------------               -----
 * [1  3  -1] -3  5  3  6  7       3
 * 1 [3  -1  -3] 5  3  6  7       3
 * 1  3 [-1  -3  5] 3  6  7       5
 * 1  3  -1 [-3  5  3] 6  7       5
 * 1  3  -1  -3 [5  3  6] 7       6
 * 1  3  -1  -3  5 [3  6  7]      7
 * 示例 2：
 * <p>
 * 输入：nums = [1], k = 1
 * 输出：[1]
 * <p>
 * 提示：
 * <p>
 * 1 <= nums.length <= 105
 * -104 <= nums[i] <= 104
 * 1 <= k <= nums.length
 * <p>
 * <p>
 * 来源：力扣（LeetCode）
 * 链接：https://leetcode.cn/problems/sliding-window-maximum
 */
public class MaxSlidingWindow239 {

    public static void main(String[] args) {

        int[] inputs = {1, 3, -1, -3, 5, 3, 6, 7};
        int[] outPut = maxSlidingWindow2(inputs, 3);

        for (int r : outPut) {
            System.out.println("max:" + r);
        }
    }


    //TODO 1.「暴力破解」 时间复杂度O n~2 比较耗时
    public static int[] maxSlidingWindow(int[] nums, int k) {

        //定义一个结果数组
        int[] result = new int[nums.length - k + 1];

        //遍历数组 从0 ~ n-k 作为滑动窗口的起始位置 nums.length - 1 = n
        for (int i = 0; i <= nums.length - k; i++) {
            int max = nums[i];
            for (int j = i + 1; j < i + k; j++) {
                if (nums[j] > max) {
                    max = nums[j];
                }
            }
            result[i] = max;
        }
        return result;
    }

    //TODO 2.「大顶堆」 优先队列 Priority Queue 最好理解
    public static int[] maxSlidingWindow2(int[] nums, int k) {

        int[] result = new int[nums.length - k + 1];

        //优先队列 默认是小顶堆需要重写比较方法 较大的出队即可.
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(k, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2 - o1;
            }
        });

        //构建大顶堆 处理每个窗口的元素
        for (int i = 0; i < k; i++) {
            maxHeap.add(nums[i]);
        }

        //当前大定堆的 堆顶元素已经有了
        result[0] = maxHeap.peek();

        //遍历所有的窗口
        for (int i = 1; i <= nums.length - k; i++) {

            //删除堆中上一个窗口的元素
            maxHeap.remove(nums[i - 1]);
            maxHeap.add(nums[i + k - 1]); //最后一个元素添加进来
            result[i] = maxHeap.peek();
        }

        return result;
    }

    //TODO 3.「双端队列」 两头控制 ArrayDeque
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

    //TODO 「左右扫描」 分治思想 算是数据方法 左右两个数组 找到最大值
    public static int[] maxSlidingWindow5(int[] nums, int k) {
        int n = nums.length;

        int[] result = new int[n - k + 1];

        //定义存放数据块内最大值的数组
        int[] left = new int[n];
        int[] right = new int[n];

        for (int i = 0; i < n; i++) {

            //从左到右
            if (i % k == 0) {
                //如果能整除 就是起始位置
                left[i] = nums[i];
            } else {
                //不是起始位置 就直接跟left[i-1] 取最大值即可.
                left[i] = Math.max(left[i - 1], nums[i]);
            }

            //从右到左 左右指针下标对称
            int j = n - 1 - i;
            if (j % k == k - 1 || j == n - 1) {
                right[j] = nums[j];
            } else {
                right[j] = Math.max(right[j + 1], nums[j]);
            }
        }

        // 对每个窗口计算最大值 合并left 和 right

        for (int i = 0; i < n - k + 1; i++) {
            result[i] = Math.max(right[i], left[i + k - 1]);
        }

        return result;
    }


    //TODO 练习
    //TODO 「双端队列」 两头控制 ArrayDeque
    public static int[] maxSlidingWindow4(int[] nums, int k) {

        //定义一个结果数组
        int[] result = new int[nums.length - k + 1];

        //定义双端队列
        ArrayDeque<Integer> deque = new ArrayDeque<>();

        //初始化双端队列
        for (int i = 0; i < k; i++) {
            while (!deque.isEmpty() && nums[i] > deque.getLast()) {
                deque.removeLast();
            }
            //只保留下标
            deque.add(i);
        }
        result[0] = nums[deque.getFirst()];

        //遍历所有元素
        for (int i = k; i < nums.length; i++) {
            //移除队顶元素
            while (!deque.isEmpty() && deque.getFirst() == i - k) {
                deque.removeFirst();
            }

            while (!deque.isEmpty() && nums[i] > deque.getLast()) {
                deque.removeLast();
            }

            deque.add(i);

            //保存元素
            result[i - k + 1] = nums[deque.getFirst()];
        }

        return result;
    }


}
