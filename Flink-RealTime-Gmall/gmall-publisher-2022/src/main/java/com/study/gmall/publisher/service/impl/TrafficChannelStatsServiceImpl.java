package com.study.gmall.publisher.service.impl;

import com.study.gmall.publisher.mapper.TrafficChannelStatsMapper;
import com.study.gmall.publisher.service.TrafficChannelStatsService;
import com.study.gmall.publisher.bean.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrafficChannelStatsServiceImpl implements TrafficChannelStatsService {

    @Autowired
    TrafficChannelStatsMapper trafficChannelStatsMapper;

    @Override
    public List<TrafficUvCt> getUvCt(Integer date) {
        return trafficChannelStatsMapper.selectUvCt(date);
    }

    @Override
    public List<TrafficSvCt> getSvCt(Integer date) {
        return trafficChannelStatsMapper.selectSvCt(date);
    }

    @Override
    public List<TrafficPvPerSession> getPvPerSession(Integer date) {
        return trafficChannelStatsMapper.selectPvPerSession(date);
    }

    @Override
    public List<TrafficDurPerSession> getDurPerSession(Integer date) {
        return trafficChannelStatsMapper.selectDurPerSession(date);
    }

    @Override
    public List<TrafficUjRate> getUjRate(Integer date) {
        return trafficChannelStatsMapper.selectUjRate(date);
    }
}
