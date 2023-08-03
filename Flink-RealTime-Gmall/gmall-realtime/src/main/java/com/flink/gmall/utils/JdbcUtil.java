package com.flink.gmall.utils;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.CaseFormat;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author wangziyu1
 * @Date 2022/9/1 11:23
 * @Version 1.0
 * <p>
 * 当前工具类可以适用于任何JDBC方式访问的数据库中的任何查询语句
 * 单行单列：select count(*) from t;
 * 单行多列：select * from t where id='1001'; id为主键
 * 多行单列：select name from t;
 * 多行多列：select * from t;
 */
public class JdbcUtil {

    /**
     * @param connection        连接
     * @param sql               查询语句
     * @param clz               传入的对象的class
     * @param underScoreToCamel 是否转换为驼峰转换
     * @param <T>               通用的泛型类型
     * @return
     */
    public static <T> List<T> queryList(Connection connection, String sql, Class<T> clz, boolean underScoreToCamel) throws SQLException, InstantiationException, IllegalAccessException, InvocationTargetException {

        //创建集合用于存放结果
        ArrayList<T> result = new ArrayList<>();

        //编译sql 工具类一般都给异常抛出去 谁用谁处理
        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        //执行查询
        ResultSet resultSet = preparedStatement.executeQuery();

        //查询获取元数据信息
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        //遍历结果集,将每行数据转换为T对象并加入集合   行遍历
        while (resultSet.next()) {

            //创建 T对象
            T t = clz.newInstance(); //利用反射创建一个反射的对象

            //遍历列
            for (int i = 0; i < columnCount; i++) {

                //获取列名与列值
                String columnName = metaData.getColumnName(i + 1);
                //Object value = resultSet.getObject(i); //通过下标获取值 或者下面的通过列名获取值
                Object value = resultSet.getObject(columnName);

                //判断是否需要进行下划线与驼峰进行转换
                if (underScoreToCamel) {
                    columnName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, columnName.toLowerCase());
                }
                //赋值
                BeanUtils.setProperty(t, columnName, value);
            }
            //将T对象放入集合
            result.add(t);
        }

        //关闭连接
        resultSet.close();
        preparedStatement.close();

        //返回集合
        return result;
    }

    public static void main(String[] args) throws Exception {

        DruidDataSource dataSource = DruidDSUtil.createDataSource();
        DruidPooledConnection connection = dataSource.getConnection();

        //使用 true之后可以将数据库里面的字段转换为驼峰命名
        //{"tmName":"aa","id":"1"}
        //{"tmName":"bb","id":"2"}
        List<JSONObject> jsonObjects = queryList(connection, "select count(1) ct from dim_base_trademark where id =1 ", JSONObject.class, true);
        for (JSONObject jsonObject : jsonObjects) {
            System.out.println(jsonObject);
        }

        connection.close();
    }
}
