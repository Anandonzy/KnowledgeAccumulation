package com.study.knowlages.base;


/**
 * test i,j,k
 * 看i,j,k的输出值
 */
public class SomeThingsAboutI {

    public static void main(String[] args) {
        int i = 1;
        i = i++;
        System.out.println("i="+i);
        int j = i++;
        System.out.println("j="+j);
        System.out.println("i="+i);
        int k = i + ++i * i++;
        System.out.println("i="+i);
        System.out.println(j);
        System.out.println(k);


    }
}
