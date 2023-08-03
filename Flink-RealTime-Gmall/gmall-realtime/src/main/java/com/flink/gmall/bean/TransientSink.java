package com.flink.gmall.bean;

/**
 * @Author wangziyu1
 * @Date 2022/8/29 13:33
 * @Version 1.0
 * 自定义注解 在写入ck的时候加上该注解的字段不会写入ck里面
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TransientSink {
}
