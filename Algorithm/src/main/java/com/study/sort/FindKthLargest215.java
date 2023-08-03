package com.study.sort;

import java.util.Arrays;
import java.util.Random;

/**
 * @Author wangziyu1
 * @Date 2022/7/31 17:35
 * @Version 1.0
 * 215. 数组中的第K个最大元素
 * 给定整数数组 nums 和整数 k，请返回数组中第 k 个最大的元素。
 * <p>
 * 请注意，你需要找的是数组排序后的第 k 个最大的元素，而不是第 k 个不同的元素。
 * <p>
 * <p>
 * <p>
 * 示例 1:
 * <p>
 * 输入: [3,2,1,5,6,4], k = 2
 * 输出: 5
 * 示例 2:
 * <p>
 * 输入: [3,2,3,1,2,4,5,5,6], k = 4
 * 输出: 4
 * <p>
 * <p>
 * 提示：
 * <p>
 * 1 <= k <= nums.length <= 105
 * -104 <= nums[i] <= 104
 */
public class FindKthLargest215 {


    public static void main(String[] args) {
        int[] nums = {3, 2, 1, 5, 6};
        //System.out.println(findKthLargest(nums, 2));
        System.out.println(findKthLargest3(nums, 2));


    }

    //TODO 1.「直接排序」「直接调用」 第k大 就是 n-k个
    public static int findKthLargest(int[] nums, int k) {
        Arrays.sort(nums);
        return nums[nums.length - k];
    }


    //TODO 2.「基于快速排序选择」
    //TODO 「快速选择」
    public static int findKthLargest2(int[] nums, int k) {
        return quickSelect(nums, 0, nums.length - 1, nums.length - k);
    }

    // 为了方便递归，定义一个快速选择方法
    public static int quickSelect(int[] nums, int start, int end, int index) {
        int q = randomPatition(nums, start, end);
        if (q == index) {
            return nums[q];
        } else {
            return q > index ? quickSelect(nums, start, q - 1, index) : quickSelect(nums, q + 1, end, index);
        }
    }

    // 定义一个随机分区方法
    public static int randomPatition(int[] nums, int start, int end) {
        Random random = new Random();
        int randIndex = start + random.nextInt(end - start + 1);
        swap(nums, start, randIndex);
        return partition(nums, start, end);
    }

    // 定义一个分区方法
    public static int partition(int[] nums, int start, int end) {
        int pivot = nums[start];
        int left = start;
        int right = end;
        while (left < right) {
            while (left < right && nums[right] >= pivot)
                right--;
            nums[left] = nums[right];
            while (left < right && nums[left] <= pivot)
                left++;
            nums[right] = nums[left];
        }
        nums[left] = pivot;
        return left;
    }

    // 定义一个交换元素的方法
    public static void swap(int[] nums, int i, int j) {
        int temp = nums[i];
        nums[i] = nums[j];
        nums[j] = temp;
    }


    //TODO 3.「基于堆」 用数组实现大顶堆 上浮之后 直接数组长度 n-1 即可
    public static int findKthLargest3(int[] nums, int k) {
        int n = nums.length;
        int heapSize = n;
        // 构建大顶堆
        buildMaxHeap(nums, heapSize);

        //删除 k-1 个元素 删除堆顶元素  最后一个元素 就是我们的堆顶元素¬
        for (int i = n - 1; i > n - k; i--) {
            swap(nums, 0, i);
            heapSize--;
            maxHeapify(nums, 0, heapSize);
        }

        //返回当前堆顶的元素
        return nums[0];
    }

    /**
     * 定义一个调整成大顶堆的方法
     *
     * @param nums
     * @param top
     * @param heapSize
     */
    private static void maxHeapify(int[] nums, int top, int heapSize) {
        //定义左右子节点
        int left = top * 2 + 1;
        int right = top * 2 + 2;

        //保存一下最大元素的索引位置
        int largest = top;

        if (right < heapSize && nums[right] > nums[largest])
            largest = right;

        if (right < heapSize && nums[left] < nums[largest])
            largest = left;

        //将最大元素换到队顶
        if (largest != top) {
            swap(nums, top, largest);

            //递归调用 继续下沉
            maxHeapify(nums, largest, heapSize);
        }
    }


    /**
     * 构建大顶堆 从下到上 n/2-1 ~ 0
     *
     * @param nums
     * @param heapSize
     */
    private static void buildMaxHeap(int[] nums, int heapSize) {
        for (int i = heapSize / 2 - 1; i > 0; i--) {
            maxHeapify(nums, i, heapSize);
        }
    }
}
