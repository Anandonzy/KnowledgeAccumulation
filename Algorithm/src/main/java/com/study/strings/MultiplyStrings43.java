package com.study.strings;

/**
 * @Author wangziyu1
 * @Date 2022/7/12 10:28
 * @Version 1.0
 * <p>
 * 给定两个以字符串形式表示的非负整数 num1和 num2，返回 num1 和 num2 的乘积，它们的乘积也表示为字符串形式。
 * <p>
 * 注意：不能使用任何内置的 BigInteger 库或直接将输入转换为整数。
 * <p>
 * <p>
 * 示例 1:
 * <p>
 * 输入: num1 = "2", num2 = "3"
 * 输出: "6"
 * 示例2:
 * <p>
 * 输入: num1 = "123", num2 = "456"
 * 输出: "56088"
 * <p>
 * 提示：
 * <p>
 * 1 <= num1.length, num2.length <= 200
 * num1 和 num2 只能由数字组成。
 * num1 和 num2 都不包含任何前导零，除了数字0本身。
 * <p>
 * 来源：力扣（LeetCode）
 * 链接：https://leetcode.cn/problems/multiply-strings
 */
public class MultiplyStrings43 {

    public static void main(String[] args) {
        System.out.println(multiply2("123", "456"));
    }


    // TODO 1.字符串相乘 num2 * num1的每一位 然后补0相加 基于字符串相加
    public static String multiply(String num1, String num2) {

        //特殊情况
        if (num1.equals("0") || num2.equals("0")) {
            return "0";
        }

        //一般情况
        //定义是粗结果 直接定义String 调用字符串相加方法
        String result = "0";

        //1 2 3
        //4 5 6
        //-------
        //  先遍历num2 第一位跟 num1全部相乘 然后进位

        //从个位开始遍历num2 的每一位,跟num1相乘 , 并且加计算结果
        for (int i = num2.length() - 1; i >= 0; i--) {
            int n2 = num2.charAt(i) - '0';
            //定义StringBuffer 保存计算结果
            StringBuffer curResult = new StringBuffer();
            int carry = 0; //定义进位

            //1.因为结果是倒序的,所以当前n2对应的数位要进行补0,应该先写入curResult 补n-1-j个0
            for (int j = 0; j < num2.length() - 1 - i; j++) {
                curResult.append("0");
            }

            //遍历num1的每一位进行相乘
            for (int j = num1.length() - 1; j >= 0; j--) {
                int n1 = num1.charAt(j) - '0';
                int product = n1 * n2 + carry;
                curResult.append(product % 10);
                carry = product / 10;
            }
            //3. 所有数位乘法计算完毕之后,如果有进位,需要将进位单独作为一位保存下来
            if (carry != 0) {
                curResult.append(carry);
            }

            //现在得到的最终乘积
            result = AddStrings451.addStrings(result, curResult.reverse().toString());
        }

        return result;
    }

    // TODO 字符串相乘 num2 * num1的每一位 然后直接写结果 最终所有结果相加
    //竖式相加 方法优化
    public static String multiply2(String num1, String num2) {
        if (num1.equals("0") || num2.equals("2")) {
            return "0";
        }
        //定义一个int 类型的数组保存计算结果的每一位
        int[] resultArray = new int[num1.length() + num2.length()];

        for (int i = num1.length() - 1; i >= 0; i--) {
            int n1 = num1.charAt(i) - '0';
            for (int j = num2.length() - 1; j >= 0; j--) {
                int n2 = num2.charAt(j) - '0';
                int product = n1 * n2;
                int temp = resultArray[i + j + 1] + product;
                resultArray[i + j + 1] = temp % 10; //保留各位
                resultArray[i + j] += temp / 10; //保留十位
            }
        }

        StringBuffer result = new StringBuffer();
        int start = resultArray[0] == 0 ? 1 : 0;
        for (int k = start; k < resultArray.length; k++) {
            result.append(resultArray[k]);
        }

        return result.toString();
    }
}
