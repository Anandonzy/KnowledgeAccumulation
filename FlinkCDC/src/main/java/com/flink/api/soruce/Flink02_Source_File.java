package com.flink.api.soruce;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @version 1.0
 * @Author wangziyu1
 * @Date 9/17/21 10:47 AM
 * @注释
 * 说明:
 * 1.	参数可以是目录也可以是文件
 * 2.	路径可以是相对路径也可以是绝对路径
 * 3.	相对路径是从系统属性user.dir获取路径: idea下是project的根目录, standalone模式下是集群节点根目录
 * 4.	也可以从hdfs目录下读取, 使用路径:hdfs://...., 由于Flink没有提供hadoop相关依赖, 需要pom中添加相关依赖:
 * <dependency>
 *     <groupId>org.apache.hadoop</groupId>
 *     <artifactId>hadoop-client</artifactId>
 *     <version>3.1.3</version>
 *     <scope>provided</scope>
 * </dependency>
 */
public class Flink02_Source_File {

    public static void main(String[] args) throws Exception {

        // 1. 创建执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env
                .readTextFile("input")
                .print();

        env.execute();
    }
}
