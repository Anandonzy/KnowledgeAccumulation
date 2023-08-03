package com.study.gmall.publisher.service;

import com.study.gmall.publisher.bean.TrafficVisitorStatsPerHour;
import com.study.gmall.publisher.bean.TrafficVisitorTypeStats;

import java.util.List;

public interface TrafficVisitorStatsService {
    List<TrafficVisitorTypeStats> getVisitorTypeStats(Integer date);

    List<TrafficVisitorStatsPerHour> getVisitorPerHrStats(Integer date);
}
