package com.flink.gmall.app.dwd;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.flink.gmall.utils.MyKafkaUtil;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.PatternTimeoutFunction;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.OutputTag;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * @Author wangziyu1
 * @Date 2022/8/18 10:24
 * @Version 1.0
 *
 * 流量域用户跳出事务事实表
 *
 * 跳出页面明细 发到kafka
 * 使用状态+CEP+within窗口
 * 生产者: bin/kafka-console-producer.sh --broker-list 192.168.200.144:9092 --topic  dwd_traffic_page_log_new
 * 消费者:
 * 测试数据:
 * 1.带last_page_id 不会任何输出
 * {"common":{"ar":"370000","ba":"Xiaomi","ch":"web","is_new":"0","md":"Xiaomi 10 Pro ","mid":"mid_2190279","os":"Android 11.0","uid":"688","vc":"v2.1.134"},"page":{"during_time":11863,"item":"34,27,14","item_type":"sku_ids","last_page_id":"cart","page_id":"trade"},"ts":1651303991000}
 * 2. 不带last_page_id 但是窗口还没达到水位线 就不会出发计算 ts:1651303991000
 * {"common":{"ar":"370000","ba":"Xiaomi","ch":"web","is_new":"0","md":"Xiaomi 10 Pro ","mid":"mid_2190279","os":"Android 11.0","uid":"688","vc":"v2.1.134"},"page":{"during_time":11863,"item":"34,27,14","item_type":"sku_ids","page_id":"trade"},"ts":1651303991000}
 * 3.  不带last_page_id 窗口刚好达到水位线会触发计算 ts:1651304100000 输出上一跳数据2
 * {"common":{"ar":"370000","ba":"Xiaomi","ch":"web","is_new":"0","md":"Xiaomi 10 Pro ","mid":"mid_2190279","os":"Android 11.0","uid":"688","vc":"v2.1.134"},"page":{"during_time":11863,"item":"34,27,14","item_type":"sku_ids","page_id":"trade"},"ts":1651304100000}
 * 4. 在加15秒 ts:1651304115000 mid 改变了 watermark 是向下游广播的 「水位线的传播不会根据key影响的」 这个地方时间超过了水位上一跳数据3 还是会输出
 * {"common":{"ar":"370000","ba":"Xiaomi","ch":"web","is_new":"0","md":"Xiaomi 10 Pro ","mid":"mid_2190280","os":"Android 11.0","uid":"688","vc":"v2.1.134"},"page":{"during_time":11863,"item":"34,27,14","item_type":"sku_ids","page_id":"trade"},"ts":1651304115000}
 * 5.时间在加2s  mid 没变 ,ts:1651304117000 此时数据不会输出  此时watermark 是15 这个时候就可能出现16.5 这样的数据,  所以数据就不能输出
 * {"common":{"ar":"370000","ba":"Xiaomi","ch":"web","is_new":"0","md":"Xiaomi 10 Pro ","mid":"mid_2190280","os":"Android 11.0","uid":"688","vc":"v2.1.134"},"page":{"during_time":11863,"item":"34,27,14","item_type":"sku_ids","page_id":"trade"},"ts":1651304117000}
 * 6.时间在加1s  mid 没变 ,ts:1651304118000 此时数据还是不会输出  此时watermark 是16 这个时候就可能出现16.5 这样的数据,  所以数据就不能输出
 * {"common":{"ar":"370000","ba":"Xiaomi","ch":"web","is_new":"0","md":"Xiaomi 10 Pro ","mid":"mid_2190280","os":"Android 11.0","uid":"688","vc":"v2.1.134"},"page":{"during_time":11863,"item":"34,27,14","item_type":"sku_ids","page_id":"trade"},"ts":1651304118000}
 * 7.时间在改变  ts:1651304120000 此时数据输出4
 * {"common":{"ar":"370000","ba":"Xiaomi","ch":"web","is_new":"0","md":"Xiaomi 10 Pro ","mid":"mid_2190280","os":"Android 11.0","uid":"688","vc":"v2.1.134"},"page":{"during_time":11863,"item":"34,27,14","item_type":"sku_ids","page_id":"trade"},"ts":1651304120000}
 *
 * 这个地方就能证明CEP+开窗可以处理乱序数据问题
 * WaterMark可以结合开窗处理乱序数据,表示小于WaterMark数据已经到齐！
 *
 */
//数据流：web/app -> Nginx -> 日志服务器(.log) -> Flume -> Kafka(ODS) -> FlinkApp -> Kafka(DWD) -> FlinkApp -> Kafka(DWD)
//程  序：     Mock(lg.sh) -> Flume(f1) -> Kafka(ZK) -> BaseLogApp -> Kafka(ZK) -> DwdTrafficUserJumpDetail -> Kafka(ZK)
public class DwdTrafficUserJumpDetail {

    public static void main(String[] args) throws Exception {

        //TODO 1.初始化环境
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

        //TODO 2.读取kafka的数据
        String topic = "dwd_traffic_page_log_new";
        String groupId = "user_jump_detail_test";
        DataStreamSource<String> kafkaDS = env.addSource(MyKafkaUtil.getFlinkKafkaConsumer(topic, groupId));

        //TODO 3.每行数据转换为JSONObject
        SingleOutputStreamOperator<JSONObject> jsonObj = kafkaDS.map(JSON::parseObject);

        //TODO 4.提取事件时间 & mid分组 水位线提取时间戳一定要放到keyby之前 否则返回的数据流就不是键控数据流了
        KeyedStream<JSONObject, String> keyedStream = jsonObj
                .assignTimestampsAndWatermarks(WatermarkStrategy.<JSONObject>forBoundedOutOfOrderness(Duration.ofSeconds(2)).withTimestampAssigner(new SerializableTimestampAssigner<JSONObject>() {
                    @Override
                    public long extractTimestamp(JSONObject element, long recordTimestamp) {
                        return element.getLong("ts");
                    }
                })).keyBy(json -> json.getJSONObject("common").getString("mid"));
        //如果keyby之后调用的提取事件的话 就是返回的SingleOutputStreamOperator 不是KeyedStream 而且只能使用windowAll窗口
        //SingleOutputStreamOperator<JSONObject> streamOperator = jsonObj.keyBy(json -> json.getJSONObject("common").getString("mid")).assignTimestampsAndWatermarks();
        //streamOperator.windowAll()

        //TODO 5.定义CEP模式序列
        Pattern<JSONObject, JSONObject> pattern = Pattern.<JSONObject>begin("start").where(new SimpleCondition<JSONObject>() {
            @Override
            public boolean filter(JSONObject value) throws Exception {

                return value.getJSONObject("page").getString("last_page_id") == null;
            }
        }).next("next").where(new SimpleCondition<JSONObject>() {
            @Override
            public boolean filter(JSONObject value) throws Exception {
                return value.getJSONObject("page").getString("last_page_id") == null;
            }
        }).within(Time.seconds(10L));
        //使用time循环模式 代替上述代码 「注意加上consecutive」 蚕食严格紧邻
        //Pattern.<JSONObject>begin("start").where(new SimpleCondition<JSONObject>() {
        //            @Override
        //            public boolean filter(JSONObject value) throws Exception {
        //
        //                return value.getJSONObject("page").getString("last_page_id") == null;
        //            }
        //        }).times(2) //默认是宽松紧邻
        //        .consecutive()// 需要加上这个 才是严格紧邻
        //        .within(Time.seconds(10));

        //TODO 6.将模式序列作用到流上
        PatternStream<JSONObject> patternStream = CEP.pattern(keyedStream, pattern);
        //TODO 7.提取事件(匹配上的事件) 以及 超时事件
        OutputTag<String> timeOutTag = new OutputTag<String>("timeOut") {
        };
        SingleOutputStreamOperator<String> selectDS = patternStream.select(timeOutTag, new PatternTimeoutFunction<JSONObject, String>() {

            /**
             * 这个地方思考一下为啥使用的 是List集合 key 就是我们匹配事件的key
             * 由于上边分析了这个地方可能有time() 循环多次 所以可能会有多个数据
             */
            @Override
            public String timeout(Map<String, List<JSONObject>> map, long l) throws Exception {
                return map.get("start").get(0).toJSONString(); //超时之后是我们想要的数据
            }
        }, new PatternSelectFunction<JSONObject, String>() {
            @Override
            public String select(Map<String, List<JSONObject>> map) throws Exception {
                return map.get("start").get(0).toJSONString(); //也取出来第一个就是我们要的跳出明细数据
            }
        });
        DataStream<String> timeOutDS = selectDS.getSideOutput(timeOutTag);

        //TODO 8.合并两个事件
        DataStream<String> unionDS = selectDS.union(timeOutDS);

        //TODO 9.数据写到kafka
        selectDS.print("Select>>>>>>>");
        timeOutDS.print("TimeOut>>>>>");
        String targetTopic = "dwd_traffic_user_jump_detail";
        unionDS.addSink(MyKafkaUtil.getFlinkKafkaProducer(targetTopic));

        //TODO 10.启动任务
        env.execute("DwdTrafficUserJumpDetail");
    }


}
