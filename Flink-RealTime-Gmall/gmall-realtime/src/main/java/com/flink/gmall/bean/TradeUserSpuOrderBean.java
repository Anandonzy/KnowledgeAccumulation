package com.flink.gmall.bean;

/**
 * @Author wangziyu1
 * @Date 2022/9/1 09:54
 * @Version 1.0
 * 带自定义注解的对象
 * 使用构造者设计模式
 * 加上@Builder对象
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
@Builder
public class TradeUserSpuOrderBean {
    // 窗口起始时间
    //@Builder.Default //构造者设计模式设置初始默认值
    String stt="111";
    // 窗口结束时间
    String edt;
    // 品牌 ID
    String trademarkId;
    // 品牌名称
    String trademarkName;
    // 一级品类 ID
    String category1Id;
    // 一级品类名称
    String category1Name;
    // 二级品类 ID
    String category2Id;
    // 二级品类名称
    String category2Name;
    // 三级品类 ID
    String category3Id;
    // 三级品类名称
    String category3Name;

    // 订单 ID
    @TransientSink
    Set<String> orderIdSet;

    // sku_id
    @TransientSink
    String skuId;

    // 用户 ID
    String userId;
    // spu_id
    String spuId;
    // spu 名称
    String spuName;
    // 下单次数
    Long orderCount;
    // 下单金额
    Double orderAmount;
    // 时间戳
    Long ts;

    public static void main(String[] args) {
        /**
         * 构造者设计模式 需要哪些字段就初始化哪些字段 其他的都是空即可
         */
        TradeUserSpuOrderBean userSpuOrderBean = TradeUserSpuOrderBean.builder()
                .skuId("1")
                .userId("2")
                .build();

        System.out.println(userSpuOrderBean);
        System.out.println(userSpuOrderBean.getSkuId());
        System.out.println(userSpuOrderBean.getOrderAmount());
    }
}
