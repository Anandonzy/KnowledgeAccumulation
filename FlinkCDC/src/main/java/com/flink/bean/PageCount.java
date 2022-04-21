package com.flink.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @version 1.0
 * @Author wangziyu1
 * @Date 10/15/21 2:52 PM
 * @注释
 */



@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageCount {
    private String url;
    private Long count;
    private Long windowEnd;
}
