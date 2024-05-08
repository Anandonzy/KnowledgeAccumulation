package com.study.binarysearch;


/**
 * @Author wangziyu1
 * @Date 2022/7/8 16:44
 * @Version 1.0
 * 编写一个高效的算法来判断 m x n 矩阵中，是否存在一个目标值。该矩阵具有如下特性：
 * <p>
 * 每行中的整数从左到右按升序排列。
 * 每行的第一个整数大于前一行的最后一个整数。
 * <p>
 * 示例 1：
 * <p>
 * <p>
 * 输入：matrix = [[1,3,5,7],[10,11,16,20],[23,30,34,60]], target = 3
 * 输出：true
 * 示例 2：
 * <p>
 * <p>
 * 输入：matrix = [[1,3,5,7],[10,11,16,20],[23,30,34,60]], target = 13
 * 输出：false
 * <p>
 * 提示：
 * <p>
 * m == matrix.length
 * n == matrix[i].length
 * 1 <= m, n <= 100
 * -104 <= matrix[i][j], target <= 104
 * <p>
 * 来源：力扣（LeetCode）
 * 链接：https://leetcode.cn/problems/search-a-2d-matrix
 */
public class SearchMatrix74 {

    public static void main(String[] args) {

        int input[][] = new int[][]{
                {1, 3, 5, 7},
                {10, 11, 16, 20},
                {23, 30, 34, 60}
        };
        System.out.println(searchMatrix2(input, 3));
    }


    //TODO 二分查找解决
    public static boolean searchMatrix(int[][] matrix, int target) {

        //定义m和n
        int m = matrix.length;
        if (m == 0) return false;
        int n = matrix[0].length;

        //二分查找 定义左右指针
        int left = 0;
        int right = m * n - 1;
        while (left <= right) {
            //计算中间位置
            int mid = (left + right) / 2;
            //计算二维矩阵中对应的行列号 ,取出来元素
            // row = index/n , col = index%n
            int midElement = matrix[mid / n][mid % n];

            if (target > midElement) {
                left = mid + 1;
            } else if (midElement > target) {
                right = mid - 1;
            } else {
                return true;
            }
        }
        return false;
    }

    //TODO 练习
    public static boolean searchMatrix2(int[][] matrix, int target) {


        int m = matrix.length;
        if (0 == m) return false;
        int n = matrix[0].length;

        int left = 0;
        int right = m * n - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;

            int midElement = matrix[mid / n][mid % n];

            if (target < midElement) {
                right = mid - 1;

            } else if (target > midElement) {
                left = mid + 1;
            } else {
                return true;
            }
        }
        return false;

    }


}
