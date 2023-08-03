package com.study.sort;

/**
 * @Author wangziyu1
 * @Date 2022/7/30 16:52
 * @Version 1.0
 * 希尔排序
 * 对于直接插入排序问题，数据量巨大时。
 * <p>
 * 将数的个数设为n，取奇数k=n/2，将下标差值为k的数分为一组，构成有序序列。
 * 再取k=k/2 ，将下标差值为k的书分为一组，构成有序序列。
 * 重复第二步，直到k=1执行简单插入排序。
 */
public class SheelSort {

    public void sheelSort(int[] a) {
        int d = a.length;
        while (d != 0) {
            d = d / 2;
            for (int x = 0; x < d; x++) {//分的组数
                for (int i = x + d; i < a.length; i += d) {//组中的元素，从第二个数开始
                    int j = i - d;//j为有序序列最后一位的位数
                    int temp = a[i];//要插入的元素
                    for (; j >= 0 && temp < a[j]; j -= d) {//从后往前遍历。
                        a[j + d] = a[j];//向后移动d位
                    }
                    a[j + d] = temp;
                }
            }
        }
    }
}
