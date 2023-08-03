package com.study.creational.patterns.prototype;

/**
 * @author wangziyu
 * @version 1.0
 * @since 2023/7/21 16:56
 * 用于创建重复对象,同时又能保证性能稳定
 * 1. Mybatic 操作数据库,从数据库里面查出来很多记录(70% 改变很少)
 * 2. 每次查数据库,查到以后封装成一个对象返回.
 *
 * 10000 Thread 查一个记录, new User("张三",18) //每次创建一个对象并且返回
 * 系统里面就会有1w葛User 非常浪费内存.
 * 就可以设计一个缓存 缓存起来查过的保存,
 *          如果查相同的记录,还拿原来的对象
 *
 * 本质:本体给外体提供一个克隆体.
 */
public class PrototypeDemo {

    public static void main(String[] args) throws CloneNotSupportedException {

        MyBatisTest mybatis = new MyBatisTest();

        //十分危险
        //得到的是克隆体
        User zhangsan1 = mybatis.getUser("zhangsan");
        System.out.println("1==>"+zhangsan1);
        zhangsan1.setName("李四2.。。");
        System.out.println("zhangsan1自己改了："+zhangsan1);


        //得到的是克隆体
        User zhangsan2 = mybatis.getUser("zhangsan");

        System.out.println("2-->"+zhangsan2);

        //得到的是克隆体
        User zhangsan3 = mybatis.getUser("zhangsan");
        System.out.println("3-->"+zhangsan3);

        //得到的是克隆体
        User zhangsan4 = mybatis.getUser("zhangsan");
        System.out.println("4-->"+zhangsan4);

        System.out.println(zhangsan1 == zhangsan3);

    }
}
