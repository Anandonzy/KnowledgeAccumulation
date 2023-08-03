package com.study.gmall.publisher.service;

import com.study.gmall.publisher.bean.TradeProvinceOrderAmount;
import com.study.gmall.publisher.bean.TradeProvinceOrderCt;
import com.study.gmall.publisher.bean.TradeStats;

import java.util.List;

public interface TradeStatsService {
    Double getTotalAmount(Integer date);

    List<TradeStats> getTradeStats(Integer date);

    List<TradeProvinceOrderCt> getTradeProvinceOrderCt(Integer date);

    List<TradeProvinceOrderAmount> getTradeProvinceOrderAmount(Integer date);
}
