package com.flink.gmall.app.dwd;

import com.flink.gmall.utils.DateFormatUtil;
import com.flink.gmall.utils.MyKafkaUtil;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

/**
 * @Author wangziyu1
 * @Date 2022/8/17 11:27
 * @Version 1.0
 * 日志主题数据分流
 */

//数据流：web/app -> Nginx -> 日志服务器(.log) -> Flume -> Kafka(ODS) -> FlinkApp -> Kafka(DWD)
//程  序：     Mock(lg.sh) -> Flume(f1) -> Kafka(ZK) -> BaseLogApp -> Kafka(ZK)
public class BaseLogApp {

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

        //TODO 2.消费Kafka topic_log 主题的数据创建流
        String topic = "topic_log";
        String groupId = "test_baselog_app";
        DataStreamSource<String> kafkaDS = env.addSource(MyKafkaUtil.getFlinkKafkaConsumer(topic, groupId));

        //TODO 3.过滤掉非JSON格式的数据&将每行数据转换为JSON对象
        OutputTag<String> dirtyTag = new OutputTag<String>("Dirty") {
        };
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaDS.process(new ProcessFunction<String, JSONObject>() {
            @Override
            public void processElement(String value, ProcessFunction<String, JSONObject>.Context ctx, Collector<JSONObject> out) throws Exception {
                try {
                    JSONObject jsonObject = JSON.parseObject(value);
                    out.collect(jsonObject);
                } catch (Exception e) {
                    ctx.output(dirtyTag, value);
                }
            }
        });
        //获取处理不了脏数据 并打印
        DataStream<String> dirtyDS = jsonObjDS.getSideOutput(dirtyTag);
        dirtyDS.print("dirtyDS>>>>>>>>>>>");


        //{"common":{"ar":"440000","ba":"iPhone","ch":"Appstore","is_new":"1","md":"iPhone Xs Max","mid":"mid_51315","os":"iOS 13.2.3","uid":"603","vc":"v2.1.132"},"start":{"entry":"notice","loading_time":1087,"open_ad_id":1,"open_ad_ms":9832,"open_ad_skip_ms":0},"ts":1651303984000}
        //TODO 4.按照Mid分组 每一个mid 都要分组
        KeyedStream<JSONObject, String> keyedStream = jsonObjDS.keyBy(json -> json.getJSONObject("common").getString("mid"));

        //TODO 5.使用状态编程做新老访客标记校验 每一个keyState都是相互隔离的
        SingleOutputStreamOperator<JSONObject> jsonObjWithFlagDS = keyedStream.map(new RichMapFunction<JSONObject, JSONObject>() {
            private ValueState<String> lastVisitState;


            //初始化 状态
            @Override
            public void open(Configuration parameters) throws Exception {
                lastVisitState = getRuntimeContext().getState(new ValueStateDescriptor<String>("last-visit", String.class));
            }

            @Override
            public JSONObject map(JSONObject value) throws Exception {

                //获取is_new(是否新用户字段) 字段 和 ts(时间戳字段)字段
                String isNew = value.getJSONObject("common").getString("is_new");
                Long ts = value.getLong("ts");
                String curDate = DateFormatUtil.toDate(ts);

                //获取状态中的日期
                Object lastDate = lastVisitState.value();

                //判断is_new 是否为1
                if ("1".equals(isNew)) {
                    if (lastDate == null) { //确定为新访客
                        lastVisitState.update(curDate);
                    } else if (!lastDate.equals(curDate)) {
                        value.getJSONObject("common").put("is_new", "0");
                    }
                } else if (lastDate == null) { //为0的逻辑
                    //时间改为昨天
                    lastVisitState.update(DateFormatUtil.toDate(ts - 24 * 60 * 60 * 1000L));
                }
                return value;
            }
        });

        //侧输出流只能用process
        //TODO 6.使用侧输出流进行分流处理  页面日志放到主流  启动、曝光、动作、错误放到侧输出流
        OutputTag<String> startTag = new OutputTag<String>("start") {
        };
        OutputTag<String> displayTag = new OutputTag<String>("display") {
        };
        OutputTag<String> actionTag = new OutputTag<String>("action") {
        };
        //日志样式: {"common":{"ar":"110000","ba":"Xiaomi","ch":"xiaomi","is_new":"1","md":"Xiaomi Mix2 ","mid":"mid_1818969","os":"Android 11.0","uid":"513","vc":"v2.1.134"},"err":{"error_code":2633,"msg":" Exception in thread \\  java.net.SocketTimeoutException\\n \\tat com.atgugu.gmall2020.mock.bean.log.AppError.main(AppError.java:xxxxxx)"},"start":{"entry":"notice","loading_time":12438,"open_ad_id":7,"open_ad_ms":4407,"open_ad_skip_ms":0},"ts":1651217959000}
        OutputTag<String> errorTag = new OutputTag<String>("error") {
        };
        SingleOutputStreamOperator<String> pageDS = jsonObjWithFlagDS.process(new ProcessFunction<JSONObject, String>() {
            //尝试获取错误数据
            @Override
            public void processElement(JSONObject value, ProcessFunction<JSONObject, String>.Context ctx, Collector<String> out) throws Exception {

                String err = value.getString("err");
                if (err != null) {
                    //将数据写到error侧输出流
                    ctx.output(errorTag, value.toJSONString());
                }
                //移除错误信息
                value.remove("err");

                //尝试获取启动信息
                String start = value.getString("start");

                if (start != null) {
                    ctx.output(startTag, value.toJSONString());
                } else {
                    //获取公共字段 common && 页面id && ts
                    String common = value.getString("common");
                    String pageId = value.getJSONObject("page").getString("page_id");
                    String ts = value.getString("ts");

                    //由于启动日志和曝光 和动作是互斥 的 不是启动就一定是日志或者曝光
                    JSONArray displays = value.getJSONArray("displays"); //有多条 所以数组获取
                    if (displays != null && displays.size() > 0) { //有数据
                        //遍历曝光数据 & 写到displays 侧输出流
                        for (int i = 0; i < displays.size(); i++) {
                            JSONObject display = displays.getJSONObject(i);
                            display.put("common", common);
                            display.put("page_id", pageId);
                            display.put("ts", ts);
                            ctx.output(displayTag, display.toJSONString());
                        }
                    }

                    //由于启动日志和曝光 和动作是互斥 的 不是启动就一定是日志或者曝光
                    //尝试获取动作日志
                    JSONArray actions = value.getJSONArray("actions");
                    if (actions != null && actions.size() > 0) {
                        //遍历曝光数据&写到display侧输出流
                        for (int i = 0; i < actions.size(); i++) {
                            JSONObject action = actions.getJSONObject(i);
                            action.put("common", common);
                            action.put("page_id", pageId);
                            ctx.output(actionTag, action.toJSONString());
                        }
                    }
                    //移除曝光和动作数据&写到页面日志主流
                    value.remove("displays");
                    value.remove("actions");
                    out.collect(value.toJSONString());
                }
            }
        });

        //TODO 7.提取各个侧输出流数据
        DataStream<String> startDS = pageDS.getSideOutput(startTag);
        DataStream<String> displayDS = pageDS.getSideOutput(displayTag);
        DataStream<String> actionDS = pageDS.getSideOutput(actionTag);
        DataStream<String> errorDS = pageDS.getSideOutput(errorTag);

        //TODO 8.将数据打印并写入对应的主题
        pageDS.print("Page>>>>>>>>>>");
        startDS.print("Start>>>>>>>>");
        displayDS.print("Display>>>>");
        actionDS.print("Action>>>>>>");
        errorDS.print("Error>>>>>>>>");

        String page_topic = "dwd_traffic_page_log_new";
        String start_topic = "dwd_traffic_start_log";
        String display_topic = "dwd_traffic_display_log";
        String action_topic = "dwd_traffic_action_log";
        String error_topic = "dwd_traffic_error_log";
        //TODO 8.将数据打印并写入对应的主题
        pageDS.addSink(MyKafkaUtil.getFlinkKafkaProducer(page_topic));
        startDS.addSink(MyKafkaUtil.getFlinkKafkaProducer(start_topic));
        displayDS.addSink(MyKafkaUtil.getFlinkKafkaProducer(display_topic));
        actionDS.addSink(MyKafkaUtil.getFlinkKafkaProducer(action_topic));
        errorDS.addSink(MyKafkaUtil.getFlinkKafkaProducer(error_topic));

        //TODO 9.启动任务
        env.execute("BaseLogApp");

    }


}