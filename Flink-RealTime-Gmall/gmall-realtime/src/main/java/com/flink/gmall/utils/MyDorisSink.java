package com.flink.gmall.utils;

import com.flink.gmall.bean.TrafficPageViewBean;
import org.apache.doris.flink.cfg.DorisExecutionOptions;
import org.apache.doris.flink.cfg.DorisOptions;
import org.apache.doris.flink.cfg.DorisSink;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import java.util.Properties;

/**
 * @Author wangziyu1
 * @Date 2022/9/19 19:06
 * @Version 1.0
 * json的方式写入 比较优雅 注意将json字段驼峰命名的转换为下划线命名的方式然后会根据字段写入.
 */
public class MyDorisSink<T> extends RichSinkFunction<T> {

    public static SinkFunction<String> getDorisSink(String tableName) {

        Properties pro = new Properties();
        pro.setProperty("format", "json");
        pro.setProperty("strip_outer_array", "true");
        return DorisSink.sink(
                //执行参数
                new DorisExecutionOptions.Builder()
                        .setBatchIntervalMs(2000L)
                        .setEnableDelete(false)
                        .setMaxRetries(3)
                        .setStreamLoadProp(pro)
                        .build(),
                //连接参数
                new DorisOptions.Builder()
                        .setFenodes("192.168.15.205:7030")
                        .setUsername("root")
                        .setPassword("aaaaaa")
                        .setTableIdentifier("gmall." + tableName)
                        .build());
    }


}
