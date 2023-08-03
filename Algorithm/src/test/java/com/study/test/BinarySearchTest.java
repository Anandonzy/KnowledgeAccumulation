package com.study.test;

/**
 * @Author wangziyu1
 * @Date 2023/2/14 11:08
 * @Version 1.0
 * 给定一个n个元素有序的（升序）整型数组nums和一个目标值target，写一个函数搜索nums中的target，如果目标值存在返回下标，否则返回-1。
 * 二分查找也称折半查找（Binary Search），它是一种效率较高的查找方法，前提是数据结构必须先排好序，可以在对数时间复杂度内完成查找。
 * 二分查找事实上采用的就是一种分治策略，它充分利用了元素间的次序关系，可在最坏的情况下用O（log n）完成搜索任务。
 */
public class BinarySearchTest {


    public static void main(String[] args) {


        int input[] = {1, 3, 5, 7, 9};
        System.out.println(binarySearch(input, 7));
        //System.out.println(binarySearch2(input, 7, 0, input.length - 1));


    }

    /**
     * 根据输入的数字找到对应的下标 没找到返回-1
     *
     * @param nums
     * @param target
     * @return
     */
    private static int binarySearch(int[] nums, int target) {

        //定义初始查找范围
        int low = 0;
        int high = nums.length - 1;
        //排除特殊条件 比最小的还小 比最大的还大 肯定有问题
        if (target < nums[low] || target > nums[high]) {
            return -1;
        }

        while (low < high) {
            int mid = low + (high - low) / 2; //取中间的点 防止溢出
            if (target < nums[mid]) {
                high = mid - 1;
            } else if (target > nums[mid]) {
                low = mid + 1;
            } else {
                return mid;
            }
        }


        return -1;
    }
}
