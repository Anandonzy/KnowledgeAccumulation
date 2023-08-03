package com.flink.gmall.app.dwd;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONAware;
import com.alibaba.fastjson.JSONObject;
import com.flink.gmall.utils.MyKafkaUtil;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RichFilterFunction;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * @Author wangziyu1
 * @Date 2022/8/17 16:58
 * @Version 1.0
 * 流量域独立访客事务事实表
 * 计算PV
 * 这里计算的pv的是last_page_id ==null 的数据
 * 统计mid个数即可
 * 测试:
 * bin/kafka-console-consumer.sh --bootstrap-server 192.168.200.144:9092 --topic dwd_traffic_unique_visitor_detail
 * bin/kafka-console-producer.sh --broker-list 192.168.200.144:9092 --topic  dwd_traffic_page_log_new
 * 测试日志:
 * UV明细需求测试数据:
 * {"common":{"ar":"370000","ba":"Xiaomi","ch":"web","is_new":"0","md":"Xiaomi 10 Pro ","mid":"mid_2190279","os":"Android 11.0","uid":"688","vc":"v2.1.134"},"page":{"during_time":11863,"item":"34,27,14","item_type":"sku_ids","last_page_id":"cart","page_id":"trade"},"ts":1651303991000}
 *
 * {"common":{"ar":"370000","ba":"Xiaomi","ch":"web","is_new":"0","md":"Xiaomi 10 Pro ","mid":"mid_2190279","os":"Android 11.0","uid":"688","vc":"v2.1.134"},"page":{"during_time":11863,"item":"34,27,14","item_type":"sku_ids","page_id":"trade"},"ts":1651303991000}
 *
 * {"common":{"ar":"370000","ba":"Xiaomi","ch":"web","is_new":"0","md":"Xiaomi 10 Pro ","mid":"mid_2190279","os":"Android 11.0","uid":"688","vc":"v2.1.134"},"page":{"during_time":11863,"item":"34,27,14","item_type":"sku_ids","page_id":"trade"},"ts":1651303991000}
 *
 * {"common":{"ar":"370000","ba":"Xiaomi","ch":"web","is_new":"0","md":"Xiaomi 10 Pro ","mid":"mid_2190280","os":"Android 11.0","uid":"688","vc":"v2.1.134"},"page":{"during_time":11863,"item":"34,27,14","item_type":"sku_ids","page_id":"trade"},"ts":1651303991000}
 *
 * {"common":{"ar":"370000","ba":"Xiaomi","ch":"web","is_new":"0","md":"Xiaomi 10 Pro ","mid":"mid_2190279","os":"Android 11.0","uid":"688","vc":"v2.1.134"},"page":{"during_time":11863,"item":"34,27,14","item_type":"sku_ids","page_id":"trade"},"ts":1751303991000}
 */

//数据流：web/app -> Nginx -> 日志服务器(.log) -> Flume -> Kafka(ODS) -> FlinkApp -> Kafka(DWD) -> FlinkApp -> Kafka(DWD)
//程  序：     Mock(lg.sh) -> Flume(f1) -> Kafka(ZK) -> BaseLogApp -> Kafka(ZK) -> DwdTrafficUniqueVisitorDetail -> Kafka(ZK)
public class DwdTrafficUniqueVisitorDetail {

    public static void main(String[] args) throws Exception {
        //TODO 1.获取执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1); //生产环境中设置为Kafka主题的分区数

        //1.1 开启CheckPoint
        //env.enableCheckpointing(5 * 60000L, CheckpointingMode.EXACTLY_ONCE);
        //env.getCheckpointConfig().setCheckpointTimeout(10 * 60000L);
        //env.getCheckpointConfig().setMaxConcurrentCheckpoints(2);
        //env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3, 5000L));

        //1.2 设置状态后端
        //env.setStateBackend(new HashMapStateBackend());
        //env.getCheckpointConfig().setCheckpointStorage("hdfs://hadoop102:8020/211126/ck");
        //System.setProperty("HADOOP_USER_NAME", "ziyu");

        //TODO 2. 读取kafka数据,页面主题创建流
        String topic = "dwd_traffic_page_log_new";
        String groupId = "uv_detail_test";
        DataStreamSource<String> kafkaDS = env.addSource(MyKafkaUtil.getFlinkKafkaConsumer(topic, groupId));

        //TODO 3. 过滤上一跳页面不为null的数据并将每行数据都转换为JSON对象
        SingleOutputStreamOperator<JSONObject> flatMapJsonObj = kafkaDS.flatMap(new FlatMapFunction<String, JSONObject>() {
            @Override
            public void flatMap(String value, Collector<JSONObject> collector) throws Exception {
                try {
                    JSONObject jsonObject = JSON.parseObject(value);

                    //获取上一跳的页面id
                    String lastPageId = jsonObject.getJSONObject("page").getString("last_page_id");
                    if (lastPageId == null) {
                        collector.collect(jsonObject);
                    }
                } catch (Exception e) {
                    //这里可以测流输出一下 看看多少这种数据.
                    System.err.println("解析json异常的数据" + value);
                    e.printStackTrace();
                }

            }
        });

        //TODO 4. 按照id分组
        KeyedStream<JSONObject, String> keyedStream = flatMapJsonObj.keyBy(json -> json.getJSONObject("common").getString("mid"));

        //TODO 5. 使用状态变成实现按照mid去重
        SingleOutputStreamOperator<JSONObject> uvDS = keyedStream.filter(new RichFilterFunction<JSONObject>() {
            private ValueState<String> lastVisitedState;

            //TODO 初始化状态 「优化」这里状态就保存一天即可 不用保留之前的状态
            //这里使用定时器 也可以不过不能使用filter算子了 需要使用process算子
            @Override
            public void open(Configuration parameters) throws Exception {
                //状态描述器 单拿出来了 设置状态的 TTl
                ValueStateDescriptor<String> stateDescriptor = new ValueStateDescriptor<>("last-visit", String.class);
                //状态描述器 设置TTL 以及 续命ttl的配置
                StateTtlConfig ttlConfig = new StateTtlConfig
                        .Builder(Time.days(1))
                        .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite) //写时候更新状态
                        .build();

                stateDescriptor.enableTimeToLive(ttlConfig);
                lastVisitedState = getRuntimeContext().getState(stateDescriptor);
            }

            @Override
            public boolean filter(JSONObject value) throws Exception {
                //获取状态数据 & 当前数据中的时间并且转换成日期
                String lastDate = lastVisitedState.value();
                String currDate = value.getString("ts");
                if (lastDate == null || !lastDate.equals(currDate)) { //取不到状态 或者 时间不同的时候更新一下状态
                    lastVisitedState.update(currDate); //更新状态值
                    return true;
                } else {
                    return false;
                }
            }
        });

        //TODO 6. 将数据写到kafka
        String targetTopic = "dwd_traffic_unique_visitor_detail";
        uvDS.print(">>>>>>>>");
        uvDS.map(JSONAware::toJSONString)
                .addSink(MyKafkaUtil.getFlinkKafkaProducer(targetTopic));

        //TODO 7.启动任务
        env.execute("DwdTrafficUniqueVisitorDetail");
    }
}
