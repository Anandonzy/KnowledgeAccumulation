package com.study.test;

/**
 * @Author wangziyu1
 * @Date 2023/2/13 18:39
 * @Version 1.0
 */
public class QuickSortTest {

    public static void main(String[] args) {
        int input[] = {2, 11, 3, 5, 7, 9};
        qSort2(input, 0, input.length - 1);
        for (int r :
                input) {
            System.out.println(r);
        }

    }

    /**
     * 比较传统经典的实现方式
     *
     * @param nums
     * @param start
     * @param end
     */
    public static void qSort(int[] nums, int start, int end) {
        if (start >= end ) {
            return;
        }

        int l = start;
        int r = end;


        //找一个基准点
        int pivot = nums[(start + end) / 2];

        //定义一个临时变量
        int temp = 0;
        while (l < r) {

            //在pivot 左边一直找 找到一个大于等于pivot的值,才退出
            while (nums[l] < pivot) {
                l++;
            }
            //在pivot 右边一直找 找到一个比pivot小的 才退出
            while (nums[r] > pivot) {
                r--;
            }
            //如果l>=r 说明pivot 的左右两边的值 已经按照左边全部是  比pivot的小
            //右边 都是pivot大
            if (l >= r) {
                break;
            }
            //交换
            temp = nums[l];
            nums[l] = nums[r];
            nums[r] = temp;

            //如果交换完成之后 如果发现 arr[l] == pivot 相等 -- 前移
            if (nums[l] == pivot) {
                r--;
            }

            if (nums[r] == pivot) {
                l++;
            }
        }

        //如果l ==r 必须l++ r-- 否则会栈溢出
        if (l == r) {
            l++;
            r--;
        }

        //向左递归
        if (end < r) {
            qSort(nums, start, r);
        }

        if (end > r) {
            qSort(nums, l, end);
        }

    }


    /**
     * 「使用双指针的方式实现」
     *
     * @param nums
     * @param start
     * @param end
     */
    public static void qSort2(int[] nums, int start, int end) {
        //基准条件
        if (start >= end) {
            return;
        }

        //找到一个pivot,把数组分为两部分
        int index = partition(nums, start, end);

        //递归排序 左右的两部分
        qSort2(nums, start, index - 1);
        qSort2(nums, index + 1, end);

    }

    /**
     * 分区方法 根据pivot的值分为两部分
     *
     * @param nums
     * @param start
     * @param end
     * @return
     */
    public static int partition(int[] nums, int start, int end) {

        //定义一个pivot 的值 以第一个元素为pivot值 理解成一个空位
        int pivot = nums[start];

        //定义一个双指针
        int left = start, right = end;

        while (left < right) {

            //右指针向左移动 找到一个比pivot小的值退出
            while (left < right && nums[right] >= pivot) {
                right--;
            }
            //找到之后填充最左边
            nums[left] = nums[right];
            //左指针向右移 直到找到一个比 pivot大的值退出
            while (left < right && nums[left] <= pivot) {
                left++;
            }
            //找到之后填充最右边
            nums[right] = nums[left];
        }
        //left = right 直接给pivot填入 直接返回就可以了
        nums[left] = pivot;
        return left;
    }

    private static void swap(int[] nums, int left, int right) {
        int temp = nums[left];
        nums[left] = nums[right];
        nums[right] = temp;
    }
}
