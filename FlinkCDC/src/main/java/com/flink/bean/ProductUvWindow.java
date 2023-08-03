package com.flink.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @Author wangziyu1
 * @Date 2022/11/10 16:46
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductUvWindow {
    public String productId;
    public Long count;
    public Long windowStartTime;
    public Long windowEndTime;
}
