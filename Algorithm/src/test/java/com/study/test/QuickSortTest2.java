package com.study.test;

/**
 * @Author wangziyu1
 * @Date 2023/2/14 09:49
 * @Version 1.0
 */
public class QuickSortTest2 {

    public static void main(String[] args) {


        int input[] = {2, 11, 3, 5, 7, 9};
        qSort(input, 0, input.length - 1);
        for (int r :
                input) {
            System.out.println(r);
        }
    }

    private static void qSort(int[] nums, int start, int end) {

        //基准条件
        if (start >= end) {
            return;
        }

        int mid = partition(nums, start, end);

        qSort(nums, start, mid - 1);
        qSort(nums, mid + 1, end);
    }

    /**
     * 分区方法
     *
     * @param nums
     * @param start
     * @param end
     * @return
     */
    private static int partition(int[] nums, int start, int end) {

        //以起始位置位空位 后续补齐
        int pivot = nums[start];

        //定义左右指针
        int left = start;
        int right = end;

        while (left < right) {

            while (left < right && nums[right] >= pivot) {
                right--;
            }

            nums[left] = nums[right];

            while (left < right && nums[left] <= pivot) {
                left++;
            }

            nums[right] = nums[left];
        }

        nums[left] = pivot;
        return left;
    }
}
