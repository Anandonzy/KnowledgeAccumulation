package com.study.sort;

/**
 * @Author wangziyu1
 * @Date 2022/7/30 16:36
 * @Version 1.0
 * 选择排序
 * 选择排序是一种简单直观的排序算法。
 * 它的工作原理：首先在未排序序列中找到最小（大）元素，存放到排序序列的起始位置，然后，再从剩余未排序元素中继续寻找最小（大）元素，然后追加到已排序序列的末尾。以此类推，直到所有元素均排序完毕
 */
public class SelectSort {

    /**
     * 1.第一次选择最小的放第一位
     * 2.第二次遍历选择剩下的最小的
     * 3.重复第二步，直到只剩下一个数。
     *
     * @param input
     */
    public static void selectSort(int input[]) {

        int n = input.length;
        for (int i = 0; i < n; i++) { //循环次数
            int key = input[i];
            int position = i;

            for (int j = 0; j < i + 1; j++) { //选出最小值
                if (key < input[j]) {
                    key = input[j];
                    position = j;
                }
            }
            //交换位置
            input[position] = input[i];
            input[i] = key;
        }

    }

    public static void main(String[] args) {

        int input[] = {2, 1, 3, 5, 7};
        selectSort(input);


        for (int r : input) {
            System.out.println(r);
        }
    }

}
