package com.flink.gmall.utils;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONObject;
import com.flink.gmall.common.GmallConfig;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * @Author wangziyu1
 * @Date 2022/8/17 10:06
 * @Version 1.0
 * 操作hbase的工具类
 * 目前本地没有hbase环境 先写到mysql
 */
public class PhoenixUtil {


    /**
     * 根据数据对数据进行upsert操作
     *
     * @param connection Phoenix连接
     * @param sinkTable  表名   tn
     * @param data       数据   {"id":"1001","name":"zhangsan","sex":"male"}
     *                   这个地方说明一下 一般工具类的方法进行给异常抛出去 让不同的业务调用的时候处理不同的业务逻辑
     */
    public static void upsertValues(DruidPooledConnection connection, String sinkTable, JSONObject data) throws SQLException {
        Set<String> columns = data.keySet();
        Collection<Object> values = data.values();
        String sql = "insert into " + GmallConfig.HBASE_SCHEMA + "." + sinkTable + "(" +
                StringUtils.join(columns, ",") + ") values ('" +
                StringUtils.join(values, "','") + "')";
        System.out.println("插入维表语句: " + sql);

        //预编译sql
        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        //3.执行
        preparedStatement.execute();
        //connection.commit();

        //4.释放资源
        preparedStatement.close();

    }
}
