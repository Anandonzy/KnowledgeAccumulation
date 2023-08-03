package com.study.binarysearch;

/**
 * @Author wangziyu1
 * @Date 2023/2/24 10:26
 * @Version 1.0
 * https://leetcode.cn/problems/search-rotate-array-lcci/
 */
public class SearchRotateArray {

    public static void main(String[] args) {


    }


    /**
     * // [1,2,3,4,5,6,8,9,11,12,13,15,17]
     * arr = [13,15,17,1,2,3,4,5,6,8,9,11,12]
     * left            mid             right
     * k = 10  返回 -1
     * k = 15  返回 1
     * k = 1   返回 3
     * 给定一个旋转后的数组，和一个数字k ，问数字k是否在数组中，如果在，返回数组下标，如果不在，返回-1
     *
     * @param arr
     * @param target
     * @return
     */
    public static int searchRotateArray(int[] arr, int target) {

        //获取长度
        int length = arr.length;
        int left = 0, right = length - 1;
        while (left < right) {

            int mid = (left + right) / 2;

            if (target == arr[mid]) {
                return mid;
            }
            //如果左半边有序
            //[13,15,17,1,2,3,4,5,6,8,9,11,12]
            //输入 15
            //  start         mid             end

            if (arr[left] < arr[mid]) {
                //左半边有序
                if (arr[left] <= target && target < arr[mid]) {
                    right = mid - 1;
                } else {
                    left = mid + 1;
                }
            } else if(arr[left] >= arr[mid]) {
                if (arr[right] >= target && target > arr[mid]) {
                    left = mid + 1;
                } else {
                    right = mid - 1;
                }
            }
        }
        return -1;
    }

}
