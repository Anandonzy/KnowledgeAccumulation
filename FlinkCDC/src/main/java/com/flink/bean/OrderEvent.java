package com.flink.bean;

/**
 * @version 1.0
 * @Author wangziyu1
 * @Date 9/18/21 5:28 PM
 * @注释 订单
 */
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderEvent {
    private Long orderId;
    private String eventType;
    private String txId;
    private Long eventTime;
}
