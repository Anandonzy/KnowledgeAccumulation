package com.study.sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * @Author wangziyu1
 * @Date 2022/8/2 19:39
 * @Version 1.0
 * https://leetcode.cn/problems/merge-intervals/
 * 一个简单的想法是，我们可以遍历每一个子区间，然后判断它跟其它区间是否可以合并。如果某两个区间可以合并，那么就把它们合并之后，再跟其它区间去做判断。
 * 很明显，这样的暴力算法，时间复杂度不会低于O(n^2)。有没有更好的方式呢？
 * 这里我们发现，判断区间是否可以合并的关键，在于它们左边界的大小关系。所以我们可以先把所有区间，按照左边界进行排序。
 * 那么在排完序的列表中，可以合并的区间一定是连续的。如下图所示，标记为蓝色、黄色和绿色的区间分别可以合并成一个大区间，它们在排完序的列表中是连续的：
 */
public class MergeIntervals {

    public static void main(String[] args) {

        int[][] intervals = {{1, 3}, {2, 6}, {8, 10}, {10, 18}};
        int[][] results = merge2(intervals);

        for (int[] r :
                results) {
            for (int rr :
                    r) {
                System.out.print(rr + "\t");
            }
            System.out.println();
        }

        ArrayList<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.set(list.size() - 1, 4);
        for (int r :
                list) {
            System.out.println(r);
        }
    }

    //TODO 「排序左边界」 整体思想就是先根据左边的值进行排序 然后下一个集合左边的元素如果比上一个右边的元素小就可以合并
    public static int[][] merge2(int[][] intervals) {

        //定义一个结果数组
        ArrayList<int[]> results = new ArrayList<>();

        Arrays.sort(intervals, new Comparator<int[]>() {
            @Override
            public int compare(int[] o1, int[] o2) {
                return o1[0] - o2[0];
            }
        });

        //遍历排序后的区间,逐个合并

        for (int[] interval : intervals) {

            //记录一下左右边界
            int left = interval[0], right = interval[1];

            //取出长度
            int length = results.size();

            //如果left 比最后一个右边界小的话 就可以合并 比他大就不能合并
            if (length == 0 || left > results.get(length - 1)[1]) {
                results.add(interval); //直接添加
            } else {
                //能够合并
                int mergeLeft = results.get(length - 1)[0]; //左边界
                int mergeRight = Math.max(results.get(length - 1)[1], right);
                results.set(length - 1, new int[]{mergeLeft, mergeRight}); //替换最后一个 进行合并
            }
        }
        return results.toArray(new int[results.size()][]);
    }

    //TODO 「练习」
    public static int[][] merge(int[][] intervals) {
        //定义一个结果数组
        ArrayList<int[]> results = new ArrayList<>();

        Arrays.sort(intervals, new Comparator<int[]>() {
            @Override
            public int compare(int[] o1, int[] o2) {
                return o1[0] - o2[0];
            }
        });

        //取出来每一个数组
        for (int[] interval : intervals) {

            //取出来左右边界
            int left = interval[0], right = interval[1];

            int length = results.size();

            if (length == 0 || left > results.get(length - 1)[0]) {
                results.add(interval);
            } else {
                // 新来的left < 原来的right 就可以合并了
                int mergeLeft = results.get(length - 1)[0];
                int mergeRight = Math.max(results.get(length - 1)[1], right);
                results.set(length - 1, new int[]{mergeLeft, mergeRight});
            }
        }
        return results.toArray(new int[results.size()][]);

    }

}
