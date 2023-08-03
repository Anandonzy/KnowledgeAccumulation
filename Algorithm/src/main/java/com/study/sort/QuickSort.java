package com.study.sort;

/**
 * @Author wangziyu1
 * @Date 2022/7/24 10:18
 * @Version 1.0
 * 快速排序
 * 快速排序的基本思想：通过一趟排序，将待排记录分隔成独立的两部分，其中一部分记录的关键字均比另一部分的关键字小，则可分别对这两部分记录继续进行排序，以达到整个序列有序。
 * <p>
 * 可以看出，快排也应用了分治思想，一般会用递归来实现。
 * <p>
 * 快速排序使用分治法来把一个串（list）分为两个子串（sub-lists）。具体算法描述如下：
 * <p>
 * -  从数列中挑出一个元素，称为 “基准”（pivot，中心，支点）；
 * -  重新排序数列，所有元素比基准值小的摆放在基准前面，所有元素比基准值大的摆在基准的后面（相同的数可以到任一边）。
 * -  这个称为分区（partition）操作。在这个分区退出之后，该基准就处于数列的中间位置（它应该在的位置）；
 * -  递归地（recursive）把小于基准值元素的子数列，和大于基准值元素的子数列排序。
 * <p>
 * 这里需要注意，分区操作在具体实现时，可以设置在序列首尾设置双指针，然后分别向中间移动；左指针找到最近的一个大于基准的数，右指针找到最近一个小于基准的数，然后交换这两个数。
 */
public class QuickSort {

    public static void main(String[] args) {
        int input[] = {2, 11, 3, 5, 7, 9};
        qSort(input, 0, input.length - 1);
        for (int r :
                input) {
            System.out.println(r);
        }
    }

    public static void qSort(int[] nums, int start, int end) {
        //基准条件
        if (start >= end) {
            return;
        }

        int mid = partition(nums, start, end);
        qSort(nums, start, mid - 1); //左递归
        qSort(nums, mid + 1, end); //右递归

    }

    public static int partition(int[] nums, int start, int end) {
        //选取一个基准点
        int provid = nums[start];
        int left = start;
        int right = end;
        while (left < right) {
            while (left < right && nums[right] >= provid) {
                right--;
            }
            nums[left] = nums[right];
            while (left < right && nums[left] <= provid) {
                left++;
            }
            nums[right] = nums[left];
        }

        nums[left] = provid;
        return left;
    }
}
