package com.study.knowlages.hash;

import java.io.IOException;
import java.util.Scanner;
import java.util.Set;

/**
 * @Author wangziyu1
 * @Date 2022/4/21 15:01
 * @Version 1.0
 */
public class MinHashTest {
    public static void main(String[] args) throws IOException {
        System.out.println("-----------使用k-shingle技术分析两段文本之间的Jaccard相似度-------");
        System.out.println("请输入k-shingle中k的值：");
        Scanner scann = new Scanner(System.in);
        int k = scann.nextInt();
        scann.close();
        Set<String> set = KShingle.jaccard(k);
        System.out.println("-----------使用MinHash技术分析两段文本之间的Jaccard相似度------------");
        MinHash.minHashJaccard(k, set);
        System.out.println("-----------用hash函数代替行打乱计算最小哈希签名------------");
        MinHashSignature.signatureJaccard(set, k);
    }
}
