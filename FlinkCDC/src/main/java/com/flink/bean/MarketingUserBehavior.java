package com.flink.bean;

/**
 * @version 1.0
 * @Author wangziyu1
 * @Date 9/18/21 4:03 PM
 * @注释
 */
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MarketingUserBehavior {
    private Long userId;
    private String behavior;
    private String channel;
    private Long timestamp;
}