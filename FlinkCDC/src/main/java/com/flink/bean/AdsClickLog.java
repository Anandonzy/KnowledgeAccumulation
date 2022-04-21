package com.flink.bean;

/**
 * @version 1.0
 * @Author wangziyu1
 * @Date 9/18/21 4:58 PM
 * @注释
 */
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdsClickLog {
    private Long userId;
    private Long adId;
    private String province;
    private String city;
    private Long timestamp;
}