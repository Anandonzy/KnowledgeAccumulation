package com.flink.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @version 1.0
 * @Author wangziyu1
 * @Date 10/14/21 3:47 PM
 * @注释 热门商品bean
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotItem {
    private Long itemId;
    private Long count;
    private Long windowEndTime;
}
