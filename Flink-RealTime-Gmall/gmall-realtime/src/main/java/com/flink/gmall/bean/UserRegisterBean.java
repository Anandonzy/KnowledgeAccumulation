package com.flink.gmall.bean;

/**
 * @Author wangziyu1
 * @Date 2022/8/30 15:30
 * @Version 1.0
 */
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserRegisterBean {
    // 窗口起始时间
    String stt;
    // 窗口终止时间
    String edt;
    // 注册用户数
    Long registerCt;
    // 时间戳
    Long ts;
}
