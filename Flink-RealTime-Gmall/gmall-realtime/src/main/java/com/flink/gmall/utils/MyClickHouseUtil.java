package com.flink.gmall.utils;

import com.flink.gmall.bean.TransientSink;
import com.flink.gmall.common.GmallConfig;
import lombok.SneakyThrows;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.jdbc.JdbcStatementBuilder;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @Author wangziyu1
 * @Date 2022/8/29 12:57
 * @Version 1.0
 * sink ck的工具类
 */
public class MyClickHouseUtil {

    /**
     * 在不确定泛型的情况下 先写成通用的泛型 T这个时候要给前面加个类似声明一样的操作
     * <T> SinkFunction<T>
     *
     * @param sql
     * @param <T>
     * @return
     */
    public static <T> SinkFunction<T> getSinkFunction(String sql) {


        /**
         * 反射说明:aaa属性
         * 	正常调用:Object value = obj.getAaa()
         * 	反射调用:Object value = aaa.get(obj)
         *
         * 反射说明:ccc(int a,String b)
         *     正常调用:Object value = obj.ccc(a,b)
         *     反射调用:Object value = ccc.invoke(obj,a,b)
         *
         * 表:     a,b1,c
         * Bean:   a,c,b2
         * insert into t(a,c,b1) values(?,?,?)
         * 解释:按照JavaBean的字段顺序写表的字段名称
         */
        return JdbcSink.<T>sink(sql, new JdbcStatementBuilder<T>() {
                    @SneakyThrows
                    @Override
                    public void accept(PreparedStatement preparedStatement, T t) throws SQLException {

                        //使用反射的方式获取t对象中的数据
                        Class<?> tClz = t.getClass();

//                        Method[] methods = tClz.getMethods();
//                        for (int i = 0; i < methods.length; i++) {
//                            Method method = methods[i];
//                            method.invoke(t);
//                        }

                        //获取并遍历属性
                        Field[] declaredFields = tClz.getDeclaredFields();
                        int offset = 0;
                        for (int i = 0; i < declaredFields.length; i++) {

                            //获取单个属性
                            Field field = declaredFields[i];
                            field.setAccessible(true);

                            //尝试获取字段上的自定义注解
                            TransientSink transientSink = field.getAnnotation(TransientSink.class);
                            if (transientSink != null) {
                                offset++;
                                continue;
                            }

                            //获取属性值
                            Object value = field.get(t);

                            //给占位符赋值
                            preparedStatement.setObject(i + 1 - offset, value);

                        }
                    }
                }
                , new JdbcExecutionOptions.Builder()
                        .withBatchSize(5)
                        .withBatchIntervalMs(1000L)
                        .build(),
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder().withDriverName(GmallConfig.CLICKHOUSE_DRIVER)
                        .withUrl(GmallConfig.CLICKHOUSE_URL)
                        .withUsername("default")
                        .withPassword("")
                        .build());
    }
}
