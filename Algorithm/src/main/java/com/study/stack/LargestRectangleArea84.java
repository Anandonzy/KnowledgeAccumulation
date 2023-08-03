package com.study.stack;

import java.util.Stack;

/**
 * @Author wangziyu1
 * @Date 2022/7/26 15:36
 * @Version 1.0
 */
public class LargestRectangleArea84 {


    public static void main(String[] args) {

        int height[] = {2, 1, 5, 6, 2, 3};
        System.out.println(largestRectangleArea2(height));


    }

    //TODO 1.「暴力破解」
    public static int largestRectangleArea(int[] heights) {

        //定义变量 最大面积
        int largestArea = 0;

        for (int left = 0; left < heights.length; left++) {
            //定义变量 保存高度
            int currentHeight = heights[left];

            //遍历数组 获取矩形
            for (int right = left; right < heights.length; right++) {
                //确定 当前矩形的高度
                currentHeight = (heights[right] < currentHeight) ? heights[right] : currentHeight;
                //计算当前矩形的面积
                int currentArea = (right - left + 1) * currentHeight;
                largestArea = (currentArea > largestArea) ? currentArea : largestArea;
            }
        }
        return largestArea;
    }

    //TODO 2.「双指针法」
    public static int largestRectangleArea2(int[] heights) {
        //定义变量 最大面积
        int largestArea = 0;

        //遍历整个数组
        for (int i = 0; i < heights.length; i++) {
            //保存当前的高度
            int height = heights[i];

            //定义左右指针
            int left = i, right = i;

            //分别寻找左右边界 左指针左移
            while (left >= 0) {
                if (heights[left] < height) break;
                left--;
            }

            //寻找右边界
            while (right < heights.length) {
                if (heights[right] < right) break;
                right++;
            }

            int width = right - left - 1;
            int currArea = height * width;

            largestArea = (currArea > largestArea) ? currArea : largestArea;
        }

        return largestArea;
    }

    //TODO 3.「双指针法优化」
    public static int largestRectangleArea3(int[] heights) {

        int n = heights.length;
        int[] lefts = new int[n];
        int[] rights = new int[n];
        int largestArea = 0;
        for (int i = 0; i < n; i++) {
            int height = heights[i];
            int left = i - 1;
            // 向左移动，寻找左边界
            while (left >= 0) {
                if (heights[left] < height) break;
                left = lefts[left];
            }
            lefts[i] = left;
        }
        for (int i = n - 1; i >= 0; i--) {
            int height = heights[i];
            int right = i + 1;
            // 向右移动，寻找右边界
            while (right < n) {
                if (heights[right] < height) break;
                right = rights[right];
            }
            rights[i] = right;
        }
        for (int i = 0; i < n; i++) {
            int currArea = (rights[i] - lefts[i] - 1) * heights[i];
            largestArea = currArea > largestArea ? currArea : largestArea;
        }
        return largestArea;
    }

    //TODO 4.「使用栈」
    public static int largestRectangleArea4(int[] heights) {

        int n = heights.length;
        int[] lefts = new int[n];
        int[] rights = new int[n];
        int largestArea = 0;
        // 定义一个栈，保存“候选列表”
        Stack<Integer> stack = new Stack<>();
        // 遍历所有柱子，计算左右边界
        for ( int i = 0; i < n; i++ ){
            while ( !stack.isEmpty() && heights[stack.peek()] >= heights[i] ){
                stack.pop();
            }
            lefts[i] = stack.isEmpty() ? -1 : stack.peek();
            stack.push(i);
        }
        stack.clear();
        for ( int i = n - 1; i >= 0; i-- ){
            while ( !stack.isEmpty() && heights[stack.peek()] >= heights[i] ){
                stack.pop();
            }
            rights[i] = stack.isEmpty() ? n : stack.peek();
            stack.push(i);
        }
        for ( int i = 0; i < n; i++ ){
            int currArea = ( rights[i] - lefts[i] - 1 ) * heights[i];
            largestArea = currArea > largestArea ? currArea : largestArea;
        }
        return largestArea;
    }

}
