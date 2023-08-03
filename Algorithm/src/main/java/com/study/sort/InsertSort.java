package com.study.sort;

/**
 * @Author wangziyu1
 * @Date 2022/7/30 16:50
 * @Version 1.0
 * 经常碰到这样一类排序问题：把新的数据插入到已经排好的数据列中。
 * <p>
 * 将第一个数和第二个数排序，然后构成一个有序序列
 * 将第三个数插入进去，构成一个新的有序序列。
 * 对第四个数、第五个数……直到最后一个数，重复第二步。
 */
public class InsertSort {
    public void insertSort(int[] a) {
        int length = a.length;//数组长度，将这个提取出来是为了提高速度。
        int insertNum;//要插入的数
        for (int i = 1; i < length; i++) {//插入的次数
            insertNum = a[i];//要插入的数
            int j = i - 1;//已经排序好的序列元素个数
            while (j >= 0 && a[j] > insertNum) {//序列从后到前循环，将大于insertNum的数向后移动一格
                a[j + 1] = a[j];//元素移动一格
                j--;
            }
            a[j + 1] = insertNum;//将需要插入的数放在要插入的位置。
        }
    }


}
