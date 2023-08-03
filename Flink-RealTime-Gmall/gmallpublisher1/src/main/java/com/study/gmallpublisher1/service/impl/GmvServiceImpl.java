package com.study.gmallpublisher1.service.impl;

/**
 * @Author wangziyu1
 * @Date 2022/9/6 19:08
 * @Version 1.0
 */
import com.study.gmallpublisher1.mapper.GmvMapper;
import com.study.gmallpublisher1.service.GmvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GmvServiceImpl implements GmvService {

    @Autowired
    private GmvMapper gmvMapper;

    @Override
    public BigDecimal getGmv(int date) {
        return gmvMapper.selectGmv(date);
    }

    @Override
    public Map getGmvByTm(int date, int limit) {

        //查询数据
        List<Map> mapList = gmvMapper.selectGmvByTm(date, limit);

        //创建Map存放所需要的结果数据
        HashMap<String, BigDecimal> resultMap = new HashMap<>();

        //遍历集合取出数据放入Map中
        for (Map map : mapList) {
            resultMap.put((String) map.get("trademark_name"), (BigDecimal) map.get("order_amount"));
        }

        //返回结果
        return resultMap;
    }
}
