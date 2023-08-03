package com.study.sort;

/**
 * @Author wangziyu1
 * @Date 2022/7/30 17:09
 * @Version 1.0
 * <p>
 * 快速排序练习
 */
public class QuickSort2 {

    public static void main(String[] args) {
        int input[] = {2, 11, 3, 5, 7, 9};
        qSort(input, 0, input.length - 1);
        for (int r :
                input) {
            System.out.println(r);
        }
    }

    public static void qSort(int[] nums, int start, int end) {

        //递归退出条件
        if (start >= end) {
            return;
        }

        //找到一个基准点 把数组分为两部分
        int mid = partition2(nums, start, end);

        //递归排序两部分
        qSort(nums, start, mid - 1);
        qSort(nums, mid + 1, end);
    }

    private static int partition(int[] nums, int start, int end) {

        //定义一个基准点
        int pivot = nums[start]; //以第一个元素为中心点

        //定义双指针
        int left = start, right = end;

        //开始移动
        while (left < right) {

            //TODO「必须右指针先移动」「必须先移动右指针」
            //左移右指针 找到一个比pivot小的数字 填入空位
            while (left < right && nums[right] >= pivot) {
                right--;
            }

            nums[left] = nums[right];

            // 移动左指针 找到一个比pivot大的数组 填入空位
            while (left < right && nums[left] <= pivot) {
                left++;
            }
            nums[right] = nums[left];
        }

        nums[left] = pivot;
        return left;
    }


    private static int partition2(int[] nums, int start, int end) {
        int pivot = nums[start];

        int left = start, right = end;

        //移动左右指针
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
