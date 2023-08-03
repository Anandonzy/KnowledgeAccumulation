package com.flink.gmall.bean;

/**
 * @Author wangziyu1
 * @Date 2022/8/18 17:04
 * @Version 1.0
 */
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WaterSensor2 {
    private String id;
    private String name;
    private long ts;
}
