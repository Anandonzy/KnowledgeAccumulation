package com.study.gmallpublisher1.mapper;

/**
 * @Author wangziyu1
 * @Date 2022/9/6 19:07
 * @Version 1.0
 */
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface GmvMapper {

    @Select("select sum(order_origin_total_amount) total_amount from dws_trade_order_window where toYYYYMMDD(stt)=#{date}")
    BigDecimal selectGmv(int date);

    /*
    ┌─trademark_name─┬────────────────order_amount─┐----AA----BB---
    │ 苹果           │ 867582.00000000000000000000 │
    │ TCL            │ 807914.00000000000000000000 │
    │ 华为           │ 429281.00000000000000000000 │
    │ 小米           │ 385324.00000000000000000000 │
    │ Redmi          │  94620.00000000000000000000 │
    └────────────────┴─────────────────────────────┘

    List[
       Map[(trademark_name->苹果),(order_amount->867582),(AA->),(BB->)],
       Map[(trademark_name->TCL),(order_amount->807914)],
       Map[(trademark_name->华为),(order_amount->429281)],
       ... ...
    ]
     */
    @Select("select trademark_name,sum(order_amount) order_amount from dws_trade_user_spu_order_window where toYYYYMMDD(stt)=#{date} group by trademark_name order by order_amount desc limit #{limit}")
    List<Map> selectGmvByTm(@Param("date") int date, @Param("limit") int limit);

}
