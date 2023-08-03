package com.study.gmall.publisher.service;

import com.study.gmall.publisher.bean.CategoryCommodityStats;
import com.study.gmall.publisher.bean.SpuCommodityStats;
import com.study.gmall.publisher.bean.TrademarkCommodityStats;
import com.study.gmall.publisher.bean.TrademarkOrderAmountPieGraph;

import java.util.List;
import java.util.Map;

public interface CommodityStatsService {
    List<TrademarkCommodityStats> getTrademarkCommodityStatsService(Integer date);

    List<CategoryCommodityStats> getCategoryStatsService(Integer date);

    List<SpuCommodityStats> getSpuCommodityStats(Integer date);

    List<TrademarkOrderAmountPieGraph> getTmOrderAmtPieGra(Integer date);

    Map getGmvByTm(int date, int limit);
}
