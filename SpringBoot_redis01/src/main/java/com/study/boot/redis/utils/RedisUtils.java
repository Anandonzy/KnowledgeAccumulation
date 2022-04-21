package com.study.boot.redis.utils;

/**
 * @Author wangziyu1
 * @Date 2022/3/24 11:45 上午
 * @Version 1.0
 * jedis 连接池
 */
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtils {

    private static JedisPool jedisPool;

    static {
        JedisPoolConfig jpc = new JedisPoolConfig();
        jpc.setMaxTotal(20);
        jpc.setMaxIdle(10);
        jedisPool = new JedisPool(jpc);
    }

    public static JedisPool getJedis() throws Exception{
        if(jedisPool == null)
            throw new NullPointerException("JedisPool is not OK.");
        return jedisPool;
    }

}
