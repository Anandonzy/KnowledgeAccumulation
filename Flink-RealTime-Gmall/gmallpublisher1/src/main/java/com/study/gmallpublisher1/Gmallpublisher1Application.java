package com.study.gmallpublisher1;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "com.study.gmallpublisher1.mapper")
public class Gmallpublisher1Application {

    public static void main(String[] args) {
        SpringApplication.run(Gmallpublisher1Application.class, args);
    }

}
