package com.flink.gmall.func;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONObject;
import com.flink.gmall.utils.DimUtil;
import com.flink.gmall.utils.DruidDSUtil;
import com.flink.gmall.utils.PhoenixUtil;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

import java.sql.SQLException;

/**
 * @Author wangziyu1
 * @Date 2022/8/17 10:01
 * @Version 1.0
 * Dim 表数据sink function
 * 暂时没有hbase环境 就先写到mysql 里面了
 */
public class DimSinkFunction extends RichSinkFunction<JSONObject> {
    private DruidDataSource druidDataSource = null;

    /**
     * 建立连接 从Druid连接池里面拿连接
     *
     * @param parameters
     * @throws Exception
     */
    @Override
    public void open(Configuration parameters) throws Exception {
        //如果这个地方使用的是mysql的jdbc的方式连接 超过8小时没操作就是会自动关闭 所以建议用Druid连接池.
        druidDataSource = DruidDSUtil.createDataSource();
    }

    /**
     * 处理数据写出 hbase/mysql
     * 数据格式:
     * value:{"database":"gmall-211126-flink","table":"base_trademark","type":"update","ts":1652499176,"xid":188,"commit":true,"data":{"id":13,"tm_name":"test"},"old":{"logo_url":"/aaa/aaa"},"sinkTable":"dim_xxx"}
     * value:{"database":"gmall-211126-flink","table":"order_info","type":"update","ts":1652499176,"xid":188,"commit":true,"data":{"id":13,...},"old":{"xxx":"/aaa/aaa"},"sinkTable":"dim_xxx"}
     *
     * @param value
     * @param context
     * @throws Exception
     */
    @Override
    public void invoke(JSONObject value, Context context) throws Exception {
        //1.获取连接
        DruidPooledConnection connection = druidDataSource.getConnection();

        String sinkTable = value.getString("sinkTable");
        JSONObject data = value.getJSONObject("data");

        //获取数据类型
        String type = value.getString("type");
        //如果为更新数据,则需要删除Redis中的数据
        if ("update".equals(type)) {
            DimUtil.delDimInfo(sinkTable.toUpperCase(), data.getString("id"));
        }

        //写出数据
        try {
            PhoenixUtil.upsertValues(connection, sinkTable, data);
        } catch (Exception e) {
            System.out.println("维度数据写入异常");
            e.printStackTrace();
        } finally {
            try {
                // 归还数据库连接对象
                connection.close();
            } catch (SQLException sqlException) {
                System.out.println("数据库连接对象归还异常");
                sqlException.printStackTrace();
            }

            //归还连接 并不是关闭连接
            connection.close();
        }
    }

    @Override
    public void close() throws Exception {
        druidDataSource.close();
    }
}
