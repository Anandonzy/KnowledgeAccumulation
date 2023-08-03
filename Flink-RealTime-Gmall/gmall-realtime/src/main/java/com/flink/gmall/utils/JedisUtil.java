package com.flink.gmall.utils;

/**
 * @Author wangziyu1
 * @Date 2022/9/1 16:35
 * @Version 1.0
 * Jedis 工具类
 */
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisUtil {

    private static JedisPool jedisPool;

    private static void initJedisPool() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(100);
        poolConfig.setMaxIdle(5);
        poolConfig.setMinIdle(5);
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setMaxWaitMillis(2000);
        poolConfig.setTestOnBorrow(true);
        jedisPool = new JedisPool(poolConfig, "localhost", 6379, 10000);
    }

    public static Jedis getJedis() {
        if (jedisPool == null) {
            initJedisPool();
        }
        // 获取Jedis客户端
        return jedisPool.getResource();
    }

    public static void main(String[] args) {
        Jedis jedis = getJedis();
        //jedis.select(1); //这个地方可以选择库
        String pong = jedis.ping();
        System.out.println(pong);
    }

}
