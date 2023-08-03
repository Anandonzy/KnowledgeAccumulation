package com.flink.api.soruce;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @Author wangziyu1
 * @Date 2022/11/8 09:46
 * @Version 1.0
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Sensor {
    private String sensorName;
    private double sensorValue;
}