package com.study.creational.patterns.builder;

/**
 * @author wangziyu
 * @version 1.0
 * @since 2023/8/18 17:12
 */
public class MainTest {

    public static void main(String[] args) {

        AbstractBuilder builder = new XiaomiBuilder();

//        builder.customCam("2yi");
//        builder.customCpu("晓龙888");
//        builder.customDisk("500G");
//        builder.customMem("24G");

        //链式调用

        Phone phone = builder.customCpu("骁龙8个8")
                .customCam("2亿")
                .customDisk("1T")
                .customMem("16G")
                .getProduct();
        System.out.println(phone);

        Phone build = Phone.builder()
                .cpu("1")
                .memory("2")
                .camera("3")
                .disk("4")
                .build();

        System.out.println(build);


    }
}
