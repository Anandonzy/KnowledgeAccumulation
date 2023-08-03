package com.study.dynamicprograming;

/**
 * @Author wangziyu1
 * @Date 2022/8/12 10:34
 * @Version 1.0
 */
public class BestTimeToBuyAndSellStock {

    public static void main(String[] args) {
        int input[] = {7, 1, 5, 3, 6, 4};
        int input2[] = {7, 6, 4, 3, 1};
        System.out.println(maxProfit(input));
        System.out.println(maxProfit(input2));
        System.out.println(maxProfit2(input));
        System.out.println(maxProfit2(input2));

    }

    // TODO 方法一：「暴力法」
    public static int maxProfit(int[] prices) {
        int maxPrice = 0;

        //最后一天不用卖出
        for (int i = 0; i < prices.length - 1; i++) {
            for (int j = i; j < prices.length; j++) {
                int currPrice = prices[j] - prices[i];
                maxPrice = Math.max(currPrice, maxPrice);
            }
        }
        return maxPrice;
    }

    // TODO 方法二：「动态规划」
    public static int maxProfit2(int[] prices) {
        int maxProfit = 0; //定义最大利润
        int minPrice = Integer.MAX_VALUE;

        for (int i = 0; i < prices.length; i++) {
            minPrice = Math.min(minPrice, prices[i]);
            maxProfit = Math.max(maxProfit, prices[i] - minPrice);
        }
        return maxProfit;
    }


}