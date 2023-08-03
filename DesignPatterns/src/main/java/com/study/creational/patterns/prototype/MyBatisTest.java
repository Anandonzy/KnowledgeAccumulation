package com.study.creational.patterns.prototype;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wangziyu
 * @version 1.0
 * @since 2023/7/21 17:01
 * 模拟Mybatis 创建对象
 */
public class MyBatisTest {

    //设计一下缓存
    private Map<String, User> userCache = new HashMap();


    /**
     * 从数据库里面查出来数据
     *
     * @return
     */
    public User getUser(String username) throws CloneNotSupportedException {
        User user = null;
        if (userCache.get(username) == null) {
            user = getUserInfo(username);
            userCache.put(username, user);
        } else {
            //直接缓存拿,就会有脏缓存的问题.
            //原型已经拿到,但是不能直接给
            user = userCache.get(username);

            //从这个对象中快速的得到克隆体(克隆人)
            user = (User) user.clone();
        }

        return user;
    }

    public User getUserInfo(String username) {
        System.out.println("从数据库里面查找" + username + ",并且创建对象");
        User user = new User();
        user.setName(username);
        user.setAge("18");
        userCache.put(username, user);
        return user;
    }
}
