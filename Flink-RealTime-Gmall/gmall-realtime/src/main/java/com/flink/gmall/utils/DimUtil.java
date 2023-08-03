package com.flink.gmall.utils;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.flink.gmall.common.GmallConfig;
import redis.clients.jedis.Jedis;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * @Author wangziyu1
 * @Date 2022/9/1 14:51
 * @Version 1.0
 * 查询DIM表在封装一层
 */
public class DimUtil {

    //这里的sql 还可以通过占位符的方式传递进来 不过要多个参数,几个占位符的数组
    public static JSONObject getDimInfo(Connection connection, String tableName, String key) throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {

        /**
         * Redis存储数据:String、List、Set、Hash、ZSet、Bitmap
         * 	1.存什么数据
         * 		dimInfoJsonStr
         *
         * 	2.使用什么数据类型
         * 		Hash
         * 		List
         * 		String
         *
         * 	3.RedisKey的设计
         * 		Hash:TableName id
         * 		List:TableName+id
         * 		String:TableName+id
         */
        //先查询redis 看看是否有 有的话直接返回 「这里相当于读缓存」
        String redisKey = "DIM:" + tableName + ":" + key;
        Jedis jedis = JedisUtil.getJedis();
        String dimJsonStr = jedis.get(redisKey);
        if (dimJsonStr != null) { //不为空说明redis有值直接返回即可  这里使用的是String 类型 也可以使用hash 但是不能对单独key设置过期时间.
            jedis.expire(redisKey, 24 * 60 * 60); //重置过期时间
            jedis.close();
            return JSON.parseObject(dimJsonStr);
        }

        //redis没查到 就去查Phoenix/mysql 维表
        //拼接SQL语句
        String querySql = "select * from " + GmallConfig.HBASE_SCHEMA + "." + tableName + " where id='" + key + "'";
        System.out.println("querySql>>>" + querySql);

        //查询数据
        List<JSONObject> queryList = JdbcUtil.queryList(connection, querySql, JSONObject.class, false);
        JSONObject dimInfo = queryList.get(0);

        //redis 没有数据 给我们查询到数据写到redis里面即可  没有的数据 我们给他加个写缓存 「写缓存」
        jedis.set(redisKey, dimInfo.toJSONString());
        //设置过期时间
        jedis.expire(redisKey, 24 * 60 * 60);
        //归还链接
        jedis.close();

        //返回数据.
        return dimInfo;
    }

    public static void delDimInfo(String tableName, String key) {
        //获取连接
        Jedis jedis = JedisUtil.getJedis();
        //删除数据
        jedis.del("DIM:" + tableName + ":" + key);
        //归还连接
        jedis.close();
    }

    public static void main(String[] args) throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {


        //测试工具类
        DruidDataSource dataSource = DruidDSUtil.createDataSource();
        DruidPooledConnection connection = dataSource.getConnection();

        //这里可以看到 我们查询的时候多次第二次的耗时比第一次快的多!!!
        //每一次创建连接的时候 都没有元数据信息 所以查询效率低,当我们第二次查询的时候 就不用查元数据信息了.
        long start = System.currentTimeMillis();
        JSONObject dimInfo = getDimInfo(connection, "DIM_BASE_TRADEMARK", "12");
        long end = System.currentTimeMillis();
        JSONObject dimInfo2 = getDimInfo(connection, "DIM_BASE_TRADEMARK", "13");
        long end2 = System.currentTimeMillis();

        System.out.println(dimInfo);
        System.out.println(dimInfo2);

        System.out.println(end - start);  //159  127  120  127  121  122  119
        System.out.println(end2 - end);   //8  8  8  1  1  1  1  0  0.5

        connection.close();

    }


}
