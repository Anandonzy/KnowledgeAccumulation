package com.flink.gmall.utils;

/**
 * @Author wangziyu1
 * @Date 2022/8/22 13:42
 * @Version 1.0
 * lookup 维表
 * 这里设置了缓存 ttl 如果更新维表不会立马更新.
 */
public class MysqlUtil {


    public static String getBaseDicLookUpDDL() {
        return "create table `base_dic`( " +
                "`dic_code` string, " +
                "`dic_name` string, " +
                "`parent_code` string, " +
                "`create_time` timestamp, " +
                "`operate_time` timestamp, " +
                "primary key(`dic_code`) not enforced " +
                ")" + MysqlUtil.mysqlLookUpTableDDL("base_dic");
    }

    public static String mysqlLookUpTableDDL(String tableName) {

        return " WITH ( " +
                "'connector' = 'jdbc', " +
                "'url' = 'jdbc:mysql://localhost:3306/gmall', " +
                "'table-name' = '" + tableName + "', " +
                "'lookup.cache.max-rows' = '10', " +
                "'lookup.cache.ttl' = '1 hour', " +
                "'username' = 'root', " +
                "'password' = '12345678', " +
                "'driver' = 'com.mysql.cj.jdbc.Driver' " +
                ")";
    }
}
