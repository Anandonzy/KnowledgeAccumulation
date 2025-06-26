package com.flink.gmall.app.dim;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.flink.gmall.bean.TableProcess;
import com.flink.gmall.func.DimSinkFunction;
import com.flink.gmall.func.TableProcessFunction;
import com.flink.gmall.utils.MyKafkaUtil;
import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.connectors.mysql.table.StartupOptions;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.streaming.api.datastream.BroadcastConnectedStream;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * @Author wangziyu1
 * @Date 2022/8/16 10:05
 * @Version 1.0
 * 将维度表数据写入Hbase 的 phoenix
 * 本地没有hbase的环境 暂时写到mysql里面 gmall_hbase_dim 库里面
 * 1. 读取kafka中ods层的数据
 * 2. 读取mysql中的配置表数据
 * 3. 将配置表数据广播出去
 * 4. 将ods层的数据和广播出去的配置表数据连接
 * 5. 通过连接后的数据进行维度关联
 * 6. 将关联后的数据写入mysql
 */

//数据流：web/app -> nginx -> 业务服务器 -> Mysql(binlog) -> Maxwell -> Kafka(ODS) -> FlinkApp -> Phoenix
//程  序：Mock -> Mysql(binlog) -> Maxwell -> Kafka(ZK) -> DimApp(FlinkCDC/Mysql) -> Phoenix(HBase/ZK/HDFS)
public class DimApp {
    public static void main(String[] args) throws Exception {
        //TODO 1. 创建环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        //1.1开启checkpoint
        //env.enableCheckpointing(5 * 60000L, CheckpointingMode.EXACTLY_ONCE); //多久做一次checkpoint
        //env.getCheckpointConfig().setCheckpointTimeout(10 * 60000L); //checkpoint 超时时间
        //env.getCheckpointConfig().setMaxConcurrentCheckpoints(2); //最大并行度去做checkpoint

        //1.2设置状态后端
        //env.setStateBackend(new HashMapStateBackend());
        //env.getCheckpointConfig().setCheckpointStorage("hdfs://hadoop210:8020/ck");
        //System.setProperty("HADOOP_USER_NAME", "ziyu");

        //TODO 2. 读取Kafka topic_db 主题数据创建主流
        // 读取Kafka ods_base_db 主题数据创建主流
        String topic = "topic_db";
        String groupId = "test_dim_app";
        DataStreamSource<String> kafkaDs = env.addSource(MyKafkaUtil.getFlinkKafkaConsumer(topic, groupId));

        //TODO 3.过滤掉非JSON数据&保留新增、变化以及初始化数据并将数据转换为JSON格式
        //flatMap 除了可以展平数据 还可以过滤数据
        SingleOutputStreamOperator<JSONObject> filterJsonObjDS = kafkaDs.flatMap(new FlatMapFunction<String, JSONObject>() {
            @Override
            public void flatMap(String value, Collector<JSONObject> out) throws Exception {
                try {
                    //将数据转换成JSONObject
                    JSONObject jsonObject = JSON.parseObject(value);

                    String type = jsonObject.getString("type");

                    //保留新增、变化以及初始化数据
                    if ("insert".equals(type) || "update".equals(type) || "bootstrap-insert".equals(type)) {
                        out.collect(jsonObject);
                    }
                } catch (Exception e) {
                    System.out.println("发现脏数据：" + value);
                    e.printStackTrace();
                }
            }
        });

        //TODO 4.使用FlinkCDC读取MySQL配置信息表创建配置流
        MySqlSource<String> mySqlSource = MySqlSource.<String>builder()
                .hostname("localhost")
                .port(3306)
                .username("root")
                .password("12345678")
                .databaseList("gmall_config")
                .tableList("gmall_config.table_process")
                .startupOptions(StartupOptions.initial())
                .deserializer(new JsonDebeziumDeserializationSchema())
                .build();

        DataStreamSource<String> mysqlSourceDS = env.fromSource(mySqlSource,
                WatermarkStrategy.noWatermarks(),
                "MysqlSource");

        //TODO 5. 将配置作为广播流广播出去
        MapStateDescriptor<String, TableProcess> mapStateDescriptor = new MapStateDescriptor<>("map-state", String.class, TableProcess.class);
        BroadcastStream<String> broadcastStream = mysqlSourceDS.broadcast(mapStateDescriptor);


        //TODO 6. 连接主流和广播流
        BroadcastConnectedStream<JSONObject, String> connectedStream = filterJsonObjDS.connect(broadcastStream);

        //TODO 7.处理主流数据,根据配置信息处理主流数据
        SingleOutputStreamOperator<JSONObject> dimDs = connectedStream.process(new TableProcessFunction(mapStateDescriptor));

        //TODO 8.将数据写入phoenix
        dimDs.print(">>>>>>>>>");
        dimDs.addSink(new DimSinkFunction());

        //TODO 9.启动任务
        env.execute("DimApp");
    }
}
