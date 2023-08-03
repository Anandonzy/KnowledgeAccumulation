package com.flink.gmall.bean;

/**
 * @Author wangziyu1
 * @Date 2022/8/30 16:31
 * @Version 1.0
 * 加购明细bean
 */
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CartAddUuBean {
    // 窗口起始时间
    String stt;

    // 窗口闭合时间
    String edt;

    // 加购独立用户数
    Long cartAddUuCt;

    // 时间戳
    Long ts;
}

