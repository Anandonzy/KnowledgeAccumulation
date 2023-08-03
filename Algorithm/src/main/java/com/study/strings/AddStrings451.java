package com.study.strings;

/**
 * @Author wangziyu1
 * @Date 2022/7/11 19:37
 * @Version 1.0
 */
public class AddStrings451 {

    public static void main(String[] args) {

        System.out.println(addStrings("123", "459"));


    }

    //TODO 字符串相加 取余是个位 除以10 是十位
    public static String addStrings(String num1, String num2) {
        //定义一个StringBuffer
        StringBuffer result = new StringBuffer();

        //定义遍历两个字符串的初始位置
        int i = num1.length() - 1;
        int j = num2.length() - 1;
        int carry = 0; //表示进位的字段

        //依次遍历 只要还有数位就进位
        while (i >= 0 || j >= 0 || carry != 0) {
            int n1 = i >= 0 ? num1.charAt(i) - '0' : 0;
            int n2 = j >= 0 ? num2.charAt(j) - '0' : 0;
            int sum = n1 + n2 + carry;
            result.append(sum % 10); //取余就是各位
            carry = sum / 10; //除以10 就是十位

            i--;
            j--;
        }
        return result.reverse().toString();
    }
}
