package com.flink.gmall.common;

/**
 * @Author wangziyu1
 * @Date 2022/8/16 15:33
 * @Version 1.0
 */
public class GmallConfig {

    // Phoenix库名
    //public static final String HBASE_SCHEMA = "GMALL211126_REALTIME";
    public static final String HBASE_SCHEMA = "gmall_hbase_dim";

    // Phoenix驱动
    //public static final String PHOENIX_DRIVER = "org.apache.phoenix.jdbc.PhoenixDriver";
    public static final String PHOENIX_DRIVER = "com.mysql.cj.jdbc.Driver";

    // Phoenix连接参数 暂时写到mysql 没有hbase的环境
    //public static final String PHOENIX_SERVER = "jdbc:phoenix:hadoop102,hadoop103,hadoop104:2181";
    public static final String PHOENIX_SERVER = "jdbc:mysql://localhost:3306/gmall_hbase_dim?useUnicode=true&useSSL=false&characterEncoding=utf8";

    // ClickHouse 驱动 ru.yandex.clickhouse.ClickHouseDriver
    public static final String CLICKHOUSE_DRIVER = "";

    // ClickHouse 连接 URL
    public static final String CLICKHOUSE_URL = "jdbc:clickhouse://192.168.15.208:8123/gmall";

    public static final String DORIS_SCHEMA = "gmall";

    // Doris URL
    public static final String DORIS_URL =
            "jdbc:mysql://192.168.15.205:9030/gmall?user=root&password=aaaaaa";

    // Doris 驱动
    public static final String DORIS_DRIVER = "com.mysql.cj.jdbc.Driver";


}
