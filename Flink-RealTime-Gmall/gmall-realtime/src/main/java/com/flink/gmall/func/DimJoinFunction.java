package com.flink.gmall.func;

import com.alibaba.fastjson.JSONObject;

/**
 * @Author wangziyu1
 * @Date 2022/9/5 10:24
 * @Version 1.0
 */
public interface DimJoinFunction<T>{
    String getKey(T input);

    void join(T input, JSONObject dimInfo);
}
