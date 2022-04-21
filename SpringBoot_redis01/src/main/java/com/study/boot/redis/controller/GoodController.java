package com.study.boot.redis.controller;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author wangziyu1
 * @Date 2022/3/21 5:03 下午
 * @Version 1.0
 */
@RestController
public class GoodController {

    public static final String REDIS_LOCK = "redis_lock";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Value("${server.port}")
    private String serverPort;

    @Autowired
    private Redisson redisson;

    @GetMapping("/buy_goods")
    public String buy_Goods() throws Exception {

        //String value = UUID.randomUUID().toString() + Thread.currentThread().getName();
        RLock redissonLock = redisson.getLock(REDIS_LOCK);
        redissonLock.lock();
        try {
    /*        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(REDIS_LOCK, value); //插入一个锁对象
            //设定过期时间
            stringRedisTemplate.expire(REDIS_LOCK, 10L, TimeUnit.SECONDS);*/
            //设定过期时间不是原子操作 用下面的方法
          /*  Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(REDIS_LOCK, value, 10L, TimeUnit.SECONDS);
            if (!flag) {
                return "强锁失败.";
            }*/

            String result = stringRedisTemplate.opsForValue().get("goods:001");// get key ====看看库存的数量够不够
            int goodsNumber = result == null ? 0 : Integer.parseInt(result);
            if (goodsNumber > 0) {
                int realNumber = goodsNumber - 1;
                stringRedisTemplate.opsForValue().set("goods:001", String.valueOf(realNumber));
                System.out.println("成功买到商品，库存还剩下: " + realNumber + " 件" + "\t服务提供端口" + serverPort);
                return "成功买到商品，库存还剩下:" + realNumber + " 件" + "\t服务提供端口" + serverPort;
            } else {
                System.out.println("商品已经售完/活动结束/调用超时,欢迎下次光临" + "\t服务提供端口" + serverPort);
            }

            return "商品已经售完/活动结束/调用超时,欢迎下次光临" + "\t服务提供端口" + serverPort;
        } finally {
         /*   if (stringRedisTemplate.opsForValue().get(REDIS_LOCK).equals(value)) {
                stringRedisTemplate.delete(REDIS_LOCK);
            }*/
            //由于删除的操作不是原子操作 使用redis的事务解决删除的原子操作.在不使用lua脚本的情况 使用Redis事务解决
         /*   while (true) {
                //监控我们的锁
                stringRedisTemplate.watch(REDIS_LOCK);
                if (stringRedisTemplate.opsForValue().get(REDIS_LOCK).equals(value)) {
                    stringRedisTemplate.setEnableTransactionSupport(true); //打开事务
                    stringRedisTemplate.multi();
                    stringRedisTemplate.delete(REDIS_LOCK);
                    List<Object> exec = stringRedisTemplate.exec();
                    if (null == exec) {
                        continue;
                    }
                }
                stringRedisTemplate.unwatch();
                break;
            }*/

            //使用lua脚本保证删除的原子性
           /* JedisPool jedis = RedisUtils.getJedis();
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] "
                    + "then "
                    + "    return redis.call('del', KEYS[1]) "
                    + "else "
                    + "    return 0 "
                    + "end";
            try {

                Object o = jedis.getResource().eval(script, Collections.singletonList(REDIS_LOCK),//
                        Collections.singletonList(value));

                if("1".equals(o.toString())) {
                    System.out.println("---del redis lock ok.");
                }else {
                    System.out.println("---del redis lock error.");
                }

            }finally {
                if(jedis != null)
                    jedis.close();
            }*/
            //添加后 更保险
            if(redissonLock.isLocked() && redissonLock.isHeldByCurrentThread()){
                redissonLock.unlock();
            }
        }
    }
}


