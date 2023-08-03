package com.flink.gmall.test.doris;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.doris.flink.cfg.DorisExecutionOptions;
import org.apache.doris.flink.cfg.DorisOptions;
import org.apache.doris.flink.cfg.DorisSink;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.data.StringData;
import org.apache.flink.table.types.logical.*;

import java.util.Properties;

/**
 * @Author wangziyu1
 * @Date 2022/9/19 11:41
 * @Version 1.0
 */
public class DorisStreamSinkRowDataTest {

    public static void main(String[] args) throws Exception {


        //TODO 1.获取执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1); //生产环境中设置为Kafka主题的分区数

        Properties pro = new Properties();
        pro.setProperty("format", "json");
        pro.setProperty("strip_outer_array", "true");
        LogicalType[] types = {new IntType(), new SmallIntType(), new VarCharType(), new BigIntType()};
        String[] fields = {"siteid", "citycode", "username", "pv"};
        env
                .fromElements(
                        "{\"siteid\": \"19\", \"citycode\": \"1001\", \"username\": \"ziyu\",\"pv\": \"100\"}")
                .map(new MapFunction<String, RowData>() {
                    @Override
                    public RowData map(String s) throws Exception {
                        JSONObject obj = JSON.parseObject(s);
                        GenericRowData rowData = new GenericRowData(4);
                        rowData.setField(0, obj.getIntValue("siteid"));
                        rowData.setField(1, obj.getShortValue("citycode"));
                        rowData.setField(2, StringData.fromString(obj.getString("username")));
                        rowData.setField(3, obj.getLongValue("pv"));
                        return rowData;
                    }
                })
                .addSink(DorisSink.sink(
                        fields,
                        types,
                        new DorisExecutionOptions.Builder()
                                .setBatchIntervalMs(2000L)
                                .setEnableDelete(false)
                                .setMaxRetries(3)
                                .setStreamLoadProp(pro)
                                .build(),
                        new DorisOptions.Builder()
                                .setFenodes("192.168.15.205:7030")
                                .setUsername("root")
                                .setPassword("aaaaaa")
                                .setTableIdentifier("test_db.table1")
                                .build()));

        env.execute();
    }
}
