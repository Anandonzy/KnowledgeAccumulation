package com.flink.gmall.bean;

/**
 * @Author wangziyu1
 * @Date 2022/8/30 10:05
 * @Version 1.0
 */

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

//写入doris的顺序都是我们bean字段的顺序 不然字段类型不匹配.
@Data
@AllArgsConstructor
public class TrafficPageViewBean {
    // 窗口起始时间
    String stt;
    // 窗口结束时间
    String edt;
    // app 版本号
    String vc;
    // 渠道
    String ch;
    // 地区
    String ar;
    // 新老访客状态标记 fastjson
    //@JSONField(name = "is_new") //转换json的时候 重新命名字段 需要一个个注解 比较麻烦
    String isNew ;
    //curDate 写doris的分区字段 写ck可以不用这个字段
    String curDate;
    // 独立访客数
    Long uvCt;
    // 会话数
    Long svCt;
    // 页面浏览数
    Long pvCt;
    // 累计访问时长
    Long durSum;
    // 跳出会话数
    Long ujCt;
    // 时间戳
    @TransientSink //加上这个注解可以这个子弹不写到下游 ck里面有必要可以加 doris 没有的字段不会写.
    Long ts;
}
