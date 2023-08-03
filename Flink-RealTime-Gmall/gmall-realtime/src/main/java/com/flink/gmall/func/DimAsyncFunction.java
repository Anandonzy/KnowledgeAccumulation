package com.flink.gmall.func;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONObject;
import com.flink.gmall.utils.DimUtil;
import com.flink.gmall.utils.DruidDSUtil;
import com.flink.gmall.utils.ThreadPoolUtil;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author wangziyu1
 * @Date 2022/9/2 17:50
 * @Version 1.0 TradeUserSpuOrderBean TradeUserSpuOrderBean
 * 输入输出 可以设置为T类型 做成通用的数据类型
 */
public abstract class DimAsyncFunction<T> extends RichAsyncFunction<T, T>  implements DimJoinFunction<T>{

    private DruidDataSource dataSource = null;
    private ThreadPoolExecutor threadPoolExecutor = null;
    private String tableName;

    public DimAsyncFunction() {
    }

    //除了构造方法的方式传递进来 还可以通过抽象方法子类实现
    public DimAsyncFunction(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        //获取连接池 以及线程池
        dataSource = DruidDSUtil.createDataSource();
        threadPoolExecutor = ThreadPoolUtil.getThreadPoolExecutor();

    }

    @Override
    public void asyncInvoke(T input, ResultFuture<T> resultFuture) throws Exception {

        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    //获取连接
                    DruidPooledConnection connection = dataSource.getConnection();

                    String key = getKey(input);

                    //tableName 通过构造方法传递进来
                    JSONObject dimInfo = DimUtil.getDimInfo(connection, tableName, key);

                    //将维表数据补充道当前数据里面 同理 使用抽象方法 谁调用谁补充信息
                    if (dimInfo != null) {
                        join(input, dimInfo);
                    }


                    //关闭连接
                    connection.close();

                    //将结果写出
                    resultFuture.complete(Collections.singletonList(input));
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("关联维表失败：" + input + ",Table:" + tableName);
                    //resultFuture.complete(Collections.singletonList(input));
                }
            }
        });

    }

    //模板设计模式 不同的数据关联不同的维表
    //public abstract String getKey(T intput);
    //
    //public abstract void join(T intput, JSONObject dimInfo);

    @Override
    public void timeout(T input, ResultFuture<T> resultFuture) throws Exception {
        super.timeout(input, resultFuture);
    }


}
