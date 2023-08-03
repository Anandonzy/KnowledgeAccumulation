package com.flink.gmall.utils;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author wangziyu1
 * @Date 2022/9/13 17:52
 * @Version 1.0
 */
public class MyUtils {

    public static <T> List<T> tolist(Iterable<T> it) {

        List<T> list = new ArrayList<T>();
        //it.forEach(e -> list.add(e));
        it.forEach(list::add);
        return list;
    }


}
