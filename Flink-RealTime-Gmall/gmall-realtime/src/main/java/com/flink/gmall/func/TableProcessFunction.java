package com.flink.gmall.func;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.flink.gmall.bean.TableProcess;
import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ReadOnlyBroadcastState;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.util.Collector;
import scala.annotation.meta.param;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import static com.flink.gmall.common.GmallConfig.HBASE_SCHEMA;
import static com.flink.gmall.common.GmallConfig.PHOENIX_SERVER;

/**
 * @Author wangziyu1
 * @Date 2022/8/16 14:16
 * @Version 1.0
 * DimApp 广播流和主流数据连接之后处理方法
 */
public class TableProcessFunction extends BroadcastProcessFunction<JSONObject, String, JSONObject> {

    private Connection connection;

    //由于缺少 MapStateDescriptor 我们可以声明成变量传递进来
    MapStateDescriptor<String, TableProcess> mapStateDescriptor;

    public TableProcessFunction(MapStateDescriptor<String, TableProcess> mapStateDescriptor) {
        this.mapStateDescriptor = mapStateDescriptor;
    }

    @Override
    public void open(Configuration parameters) throws Exception {

        connection = DriverManager.getConnection(PHOENIX_SERVER, "root", "12345678");
    }


    /**
     * {"before":null,"after":{"source_table":"11","sink_table":"22","sink_columns":"33","sink_pk":"44","sink_extend":"55"},"source":{"version":"1.5.4.Final","connector":"mysql","name":"mysql_binlog_source","ts_ms":1660634353000,"snapshot":"false","db":"gmall_config","sequence":null,"table":"table_process","server_id":1,"gtid":null,"file":"binlog.000051","pos":1511707,"row":0,"thread":null,"query":null},"op":"c","ts_ms":1660634353139,"transaction":null}
     *
     * @param value
     * @param ctx
     * @param out
     * @throws Exception
     */
    @Override
    public void processBroadcastElement(String value, BroadcastProcessFunction<JSONObject, String, JSONObject>.Context ctx, Collector<JSONObject> out) throws Exception {
        //1.获取并且解析数据
        JSONObject jsonObject = JSONObject.parseObject(value);
        TableProcess tableProcess = JSON.parseObject(jsonObject.getString("after"), TableProcess.class);

        //2.校验并建表
        checkTable(tableProcess.getSinkTable(), tableProcess.getSinkColumns(), tableProcess.getSinkPk(), tableProcess.getSinkExtend());

        //3.写入状态 广播出去
        BroadcastState<String, TableProcess> broadcastState = ctx.getBroadcastState(mapStateDescriptor);
        //4.表名唯一 存一条记录
        broadcastState.put(tableProcess.getSourceTable(), tableProcess);
    }

    /**
     * 校验并建表 拼接sql 编译sql 执行sql
     * 校验并建表:create table if not exists db.tn(id varchar primary key,bb varchar,cc varchar) xxx
     *
     * @param sinkTable   phoenix表名
     * @param sinkColumns phoenix表字段
     * @param sinkPk      phoenix表主键
     * @param sinkExtend  phoenix表拓展字段
     */
    private void checkTable(String sinkTable, String sinkColumns, String sinkPk, String sinkExtend) {
        PreparedStatement preparedStatement = null;

        try {
            //处理特殊字段 给默认值
            if (sinkPk == null || "".equals(sinkPk)) {
                sinkPk = "id";
            }
            if (sinkExtend == null || "".equals(sinkExtend)) {
                sinkExtend = "";
            }
            // 校验并建表:create table if not exists db.tn(id varchar primary key,bb varchar,cc varchar) xxx
            StringBuilder createTableSql = new StringBuilder("create table if not exists ")
                    .append(HBASE_SCHEMA)
                    .append(".")
                    .append(sinkTable)
                    .append("(");

            //拼接字段
            String[] columes = sinkColumns.split(",");

            for (int i = 0; i < columes.length; i++) {
                String colume = columes[i];

                if (sinkPk.equals(colume)) {
                    createTableSql.append(colume).append(" varchar(200) primary key ");
                } else {
                    createTableSql.append(colume).append(" varchar(200)");
                }

                //判断是否是最后一个字段
                if (i < columes.length - 1) {
                    createTableSql.append(",");
                }
            }
            createTableSql.append(")").append(sinkExtend);
            //编译SQL
            System.out.println("建表语句为：" + createTableSql);
            //TODO 异常考虑 这个地方考虑 如果异常程序是否停止

            preparedStatement = connection.prepareStatement(createTableSql.toString());

            //执行SQL,建表
            preparedStatement.execute();

        } catch (SQLException e) {
            e.printStackTrace();
            //TODO 「处理编译时异常」将编译时异常转换成 运行时异常
            throw new RuntimeException("建表失败：" + sinkTable);
        } finally {
            //释放资源
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 主流数据格式 也就是maxWell 监控的binlog数据 发送到kafka的Topic: topic_db 的格式 业务数据
     * 处理主流的数据
     * value:{"database":"gmall-211126-flink","table":"base_trademark","type":"update","ts":1652499176,"xid":188,"commit":true,"data":{"id":13,"tm_name":"test","logo_url":"/bbb/bbb"},"old":{"logo_url":"/aaa/aaa"}}
     *
     * @param value
     * @param ctx
     * @param out
     * @throws Exception
     */
    @Override
    public void processElement(JSONObject value, BroadcastProcessFunction<JSONObject, String, JSONObject>.ReadOnlyContext ctx, Collector<JSONObject> out) throws Exception {

        //1获取广播的配置数据
        ReadOnlyBroadcastState<String, TableProcess> broadcastState = ctx.getBroadcastState(mapStateDescriptor);

        //从主流里面取出来table 字段
        String table = value.getString("table");

        //存入状态的时候是表名为key  所以取的时候也要是从数据里面找到表名取出来状态值
        //这个地方要考虑是否有null值的情况 value里面是46个表的业务数据 可能取不到我们的dim表的数据 所以可能为null
        TableProcess tableProcess = broadcastState.get(value.getString("table"));


        if (tableProcess != null) {

            //2.过滤字段
            filterColumn(value.getJSONObject("data"), tableProcess.getSinkColumns());

            //3.补充SinkTable 并写出到流中 将这条数据写入到哪个表了补充一下
            value.put("sinkTable", tableProcess.getSinkTable());
            out.collect(value);
        } else {
            System.out.println("找不到对应的Key：" + table);
        }

    }

    /**
     * 过滤字段
     *
     * @param data        {"id":13,"tm_name":"test","logo_url":"/bbb/bbb"} 遍历数据 取出来 sinkColumns里面有的字段
     * @param sinkColumns "id,tm_name"
     */
    private void filterColumn(JSONObject data, String sinkColumns) {

        //切分 columns
        String[] columns = sinkColumns.split(",");
        List<String> columList = Arrays.asList(columns);

        //使用迭代器遍历数据 会有问题
        //Set<Map.Entry<String, Object>> entries1 = data.entrySet();
        //Iterator<Map.Entry<String, Object>> iterator = entries1.iterator();
        //while(iterator.hasNext()){
        //    Map.Entry<String, Object> next = iterator.next();
        //    if(!sinkColumns.contains(next.getKey())){ //列里面可能包含 name,tb_name contains 方法里面就都有 ,所以 sinkColumns需要切分 就不会有这个问题了.
        //        iterator.remove();
        //    }
        //}

        //使用lamda 写法 简单
        Set<Map.Entry<String, Object>> entries = data.entrySet();
        entries.removeIf(next -> !columList.contains(next.getKey()));
    }

}
