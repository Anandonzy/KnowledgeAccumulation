package com.study.array;

/**
 * @Author wangziyu1
 * @Date 2022/7/8 11:30
 * @Version 1.0
 * <p>
 * 给定一个 n × n 的二维矩阵 matrix 表示一个图像。请你将图像顺时针旋转 90 度。
 * <p>
 * 你必须在 原地 旋转图像，这意味着你需要直接修改输入的二维矩阵。请不要 使用另一个矩阵来旋转图像。
 * <p>
 * <p>
 * 示例 1：
 * 输入：matrix = [[1,2,3],[4,5,6],[7,8,9]]
 * 输出：[[7,4,1],[8,5,2],[9,6,3]]
 * 示例 2：
 * <p>
 * <p>
 * 输入：matrix = [[5,1,9,11],[2,4,8,10],[13,3,6,7],[15,14,12,16]]
 * 输出：[[15,13,2,5],[14,3,4,1],[12,6,8,9],[16,7,10,11]]
 * <p>
 * 来源：力扣（LeetCode）
 * 链接：https://leetcode.cn/problems/rotate-image
 */
public class RotateImage48 {

    public static void main(String[] args) {
        int image1[][] = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}};

        rotate3(image1);

        System.out.println("image1:");
        for (int[] line : image1) {
            for (int point : line) {
                System.out.print(point + "\t");
            }
            System.out.println();
        }

    }

    //TODO 数学方法(矩阵转置 ,再翻转每一行)
    public static void rotate(int[][] matrix) {
        int n = matrix.length;

        //1.转置矩阵
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                int temp = matrix[i][j];
                matrix[i][j] = matrix[j][i];
                matrix[j][i] = temp;
            }
        }

        //翻转每一行
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n / 2; j++) {
                int tmp = matrix[i][j];
                matrix[i][j] = matrix[i][n - 1 - j];
                matrix[i][n - 1 - j] = tmp;
            }
        }
    }

    //TODO 分治思想 分为四个子矩阵
    public static void rotate2(int[][] matrix) {

        int n = matrix.length;

        //只要遍历四分之一就好了 奇数
        for (int i = 0; i < n / 2 + n % 2; i++) {

            for (int j = 0; j < n / 2; j++) {
                //对于matrix[i][j] 需要找到不同矩阵的对应位置
                //定义一个临时数组
                int[] temp = new int[4];
                int row = i;
                int col = j;
                //行列转换的规律  当前的列变成行(col = newRow) 当前的行变成
                for (int k = 0; k < temp.length; k++) {
                    temp[k] = matrix[row][col];
                    int x = row;
                    row = col;
                    col = n - 1 - x;
                }
                //再次遍历给旋转之后的数据填入
                for (int k = 0; k < temp.length; k++) {
                    //用上一个值替换当前位置
                    matrix[row][col] = temp[(k + 3) % 4];

                    int x = row;
                    row = col;
                    col = n - 1 - x;
                }
            }
        }
    }

    //TODO 分治思想 分为四个子矩阵 优化版本
    public static void rotate3(int[][] matrix) {

        int n = matrix.length;

        //遍历四分之一
        for (int i = 0; i < (n + 1) / 2; i++) {
            for (int j = 0; j < n / 2; j++) {
                int temp = matrix[i][j];
                matrix[i][j] = matrix[n - 1 - j][i];
                matrix[n - 1 - j][i] = matrix[n - i - 1][n - j - 1];
                matrix[n - i - 1][n - j - 1] = matrix[j][n - 1 - i];
                matrix[j][n - 1 - i] = temp;
            }
        }
    }
}
