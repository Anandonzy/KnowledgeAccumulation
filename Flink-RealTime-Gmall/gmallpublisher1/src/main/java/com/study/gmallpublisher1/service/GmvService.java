package com.study.gmallpublisher1.service;

/**
 * @Author wangziyu1
 * @Date 2022/9/6 19:08
 * @Version 1.0
 */
import org.mybatis.spring.annotation.MapperScan;

import java.math.BigDecimal;
import java.util.Map;
public interface GmvService {

    //获取订单总金额
    BigDecimal getGmv(int date);

    //根据Tm获取订单总金额
    Map getGmvByTm(int date, int limit);

}
