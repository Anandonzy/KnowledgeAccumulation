package com.study.greedy;

/**
 * @Author wangziyu1
 * @Date 2022/8/10 16:10
 * @Version 1.0
 */
public class CanJumpII45 {

    public static void main(String[] args) {

        int input[] = {2, 3, 1, 1, 4};
        //int input2[] = {3, 2, 1, 0, 4};
        System.out.println(jump3(input));
        //System.out.println(jump(input2));

    }

    //TODO 1.「反向跳跃」「贪心算法」O(n`2)
    public static int jump(int[] nums) {
        //1. 定义一个变量保存跳跃步骤
        int steps = 0;

        //2.定义循环变量
        int curPosition = nums.length - 1;

        //3.不停的反向跳跃,以最远的距离跳跃
        while (curPosition > 0) {
            //4.从前到后遍历数组元素 找到当前元素最远的上一步的起跳位置
            for (int i = 0; i < curPosition; i++) {
                if (i + nums[i] >= curPosition) {
                    curPosition = i; //从前到后 第一次能跳到当前的位置就是最远的上一步的位置.
                    steps++;
                    break;
                }
            }
        }
        return steps;
    }

    //TODO 2.「正向跳跃」「贪心算法」 考虑能够跳跃的最远的两步
    public static int jump2(int[] nums) {
        //1. 定义一个变量保存跳跃步骤
        int steps = 0;

        //2.定义循环变量
        int curPosition = 0;

        //2.「双指针」 第一步最远 和 第二步最远
        int farthest = 0;
        int nextFarthest = farthest;

        //不停地寻找下一步的合适位置
        while (farthest < nums.length - 1) {
            //遍历能够 curPosition - farthest 范围内的所有元素,选择第二部跳跃最远的作为当前第一步的选择
            for (int i = curPosition; i <= farthest; i++) {
                //如果比 之前第二步距离大,则更新.
                if (i + nums[i] > nextFarthest) {
                    nextFarthest = i + nums[i];
                    curPosition = i;
                }
            }
            steps++;
            farthest = nextFarthest;
        }
        return steps;
    }

    //TODO 2.「正向跳跃」「贪心算法」 考虑能够跳跃的最远的两步 优化下
    public static int jump3(int[] nums) {
        //1. 定义一个变量保存跳跃步骤
        int steps = 0;

        //2.「双指针」 第一步最远 和 第二步最远
        int farthest = 0;
        int nextFarthest = 0;
        for (int i = 0; i < nums.length - 1; i++) {
            nextFarthest = Math.max(nextFarthest, i + nums[i]);
            if (i == farthest) {
                steps++;
                farthest = nextFarthest;
            }
        }
        return steps;
    }
}
