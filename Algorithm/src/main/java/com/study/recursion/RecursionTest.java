package com.study.recursion;

/**
 * @Author wangziyu1
 * @Date 2022/8/3 19:19
 * @Version 1.0
 * <p>
 * 递归演示
 */
public class RecursionTest {

    public static void main(String[] args) {
        System.out.println(factorial(3));
        System.out.println(fact(1,3));
    }

    // TODO「阶乘」递归示例：计算阶乘
    public static int factorial(int n) {
        //基准条件 也就是退出条件
        if (n == 0) return 1;
        return factorial(n - 1) * n;
    }

    /**
     * 这种方式 把递归调用置于函数的末尾，即正好在return语句之前，
     * 这种形式的递归被称为尾递归 (tail recursion)，其形式相当于循环。
     * 一些语言的编译器对于尾递归可以进行优化，节约递归调用的栈资源。
     * @param acc
     * @param n
     * @return
     */
    // TODO「尾递归」尾递归计算阶乘，需要多一个参数保存“计算状态”
    public static int fact(int acc, int n){
        if ( n == 0 ) return acc;
        return fact( acc * n, n - 1 );
    }

}
