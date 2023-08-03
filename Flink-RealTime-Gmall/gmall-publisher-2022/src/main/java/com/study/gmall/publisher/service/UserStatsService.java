package com.study.gmall.publisher.service;

import com.study.gmall.publisher.bean.UserChangeCtPerType;
import com.study.gmall.publisher.bean.UserPageCt;
import com.study.gmall.publisher.bean.UserTradeCt;

import java.util.List;

public interface UserStatsService {
    List<UserPageCt> getUvByPage(Integer date);

    List<UserChangeCtPerType> getUserChangeCt(Integer date);

    List<UserTradeCt> getTradeUserCt(Integer date);
}
