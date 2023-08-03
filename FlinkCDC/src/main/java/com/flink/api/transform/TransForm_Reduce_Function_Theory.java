package com.flink.api.transform;

import org.apache.flink.util.MathUtils;

/**
 * @Author wangziyu1
 * @Date 2022/11/2 16:48
 * @Version 1.0
 * reduce 算子的底层实现原理
 */
public class TransForm_Reduce_Function_Theory {

    public static void main(String[] args) {

        //计算key 为0的数据要去的Reduce 的并行子任务的索引值
        Integer i = 0;

        //1.获取key的hashCode
        int hashCode = i.hashCode();
        System.out.println("hascode:" + hashCode);

        //2. 计算key的hashCode的 murmurHash 值 一种hash算法
        int murmurHash = MathUtils.murmurHash(hashCode);
        System.out.println("murmurHash:" + murmurHash);

        // 128 是默认的最大并行度, 4 是reduce算子的并行度
        // idx 是key 为0的数据将要路由到reduce的并行子任务的索引
        //3. 第三个并行子任务的索引是2
        int idx = (murmurHash % 128) * 4 / 128;
        System.out.println("idx:" + idx);

    }
}
