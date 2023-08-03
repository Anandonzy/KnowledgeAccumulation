package com.study.binarysearch;

/**
 * @Author wangziyu1
 * @Date 2022/7/8 15:51
 * @Version 1.0
 * 经典的二分查找
 */
public class BinarySearch {

    public static void main(String[] args) {

        int input[] = {1, 3, 5, 7, 9};
        System.out.println(binarySearch(input, 7));
        System.out.println(binarySearch2(input, 7, 0, input.length - 1));


    }

    //TODO 二分查找经典例子
    public static int binarySearch(int[] a, int key) {

        //定义初始查找范围
        int low = 0;
        int high = a.length - 1;

        //排除特殊情况 不在范围内的数字
        if (key < a[low] && key > a[high]) {
            return -1;
        }

        while (low <= high) {
            int mid = low + (high - low) / 2;

            if (key < a[mid]) {
                high = mid - 1;
            } else if (key > a[mid]) {
                low = mid + 1;
            } else {
                return mid;
            }
        }
        return -1;
    }


    //TODO 递归实现二分查找
    public static int binarySearch2(int[] a, int key, int fromIndex, int toIndex) {

        //基本判断 也是递归退出条件 不然会死循环
        if (fromIndex > toIndex || key < a[fromIndex] || key > a[toIndex]) {
            return -1;
        }

        int mid = fromIndex + (toIndex - fromIndex) / 2;

        if (a[mid] < key) {
            return binarySearch2(a, key, mid + 1, toIndex);
        } else if (a[mid] > key) {
            return binarySearch2(a, key, fromIndex, mid - 1);
        } else {
            return mid;
        }
    }
}
