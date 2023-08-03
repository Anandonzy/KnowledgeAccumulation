package com.study.test;

/**
 * @Author wangziyu1
 * @Date 2023/2/24 15:32
 * @Version 1.0
 * 卖卖股票的最佳时机
 * 输入：[7,1,5,3,6,4]
 * 输出：5
 * 解释：在第 2 天（股票价格 = 1）的时候买入，在第 5 天（股票价格 = 6）的时候卖出，最大利润 = 6-1 = 5 。
 * 注意利润不能是 7-1 = 6, 因为卖出价格需要大于买入价格；同时，你不能在买入前卖出股票。
 */
public class MaxProfit {

    public static void main(String[] args) {

        int[] prices = {7, 1, 5, 3, 6, 4};
        System.out.println(maxProfit(prices));

    }

    public static int maxProfit(int[] prices) {
        int len = prices.length;
        int maxMoney = 0; //定义最大利润
        int minPrice = Integer.MAX_VALUE;


        for (int i = 0; i < len; i++) {
            minPrice = Math.min(minPrice, prices[i]);
            maxMoney = Math.max(maxMoney, prices[i] - minPrice);
        }
        return maxMoney;
    }


}
