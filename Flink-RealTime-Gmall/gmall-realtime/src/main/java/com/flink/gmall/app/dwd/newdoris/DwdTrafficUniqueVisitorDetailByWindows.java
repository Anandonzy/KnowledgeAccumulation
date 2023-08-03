package com.flink.gmall.app.dwd.newdoris;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.flink.gmall.utils.DateFormatUtil;
import com.flink.gmall.utils.MyKafkaUtil;
import com.flink.gmall.utils.MyUtils;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.time.Duration;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @Author wangziyu1
 * @Date 2022/9/13 16:32
 * @Version 1.0
 * 求UV明细数据
 * 根据uid分组开窗 然后记录状态对比登陆时间 不相等就取出来这一个窗口的最小时间的数据 就是我们的一条明细数据 然后更新状态
 */
public class DwdTrafficUniqueVisitorDetailByWindows {

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
        String groupId = "uv_detail_test_2";
        DataStreamSource<String> kafkaDS = env.addSource(MyKafkaUtil.getFlinkKafkaConsumer(topic, groupId));
        //kafkaDS.print(">>>?>>>");

        //TODO 清理数据
        SingleOutputStreamOperator<JSONObject> flatMapObjDS = kafkaDS.flatMap(new FlatMapFunction<String, JSONObject>() {
            @Override
            public void flatMap(String value, Collector<JSONObject> out) throws Exception {
                try {
                    JSONObject jsonObject = JSON.parseObject(value);
                    out.collect(jsonObject);
                } catch (Exception e) {
                    System.out.println("json数据有误:" + value);
                }
            }
        });

        //TODO 提取watermark
        SingleOutputStreamOperator<JSONObject> jsonWithWmDS = flatMapObjDS.assignTimestampsAndWatermarks(WatermarkStrategy.<JSONObject>forBoundedOutOfOrderness(Duration.ofSeconds(5)).withTimestampAssigner(new SerializableTimestampAssigner<JSONObject>() {
            @Override
            public long extractTimestamp(JSONObject jsonObject, long l) {
                return jsonObject.getLong("ts");
            }
        }));

        //TODO 分组
        KeyedStream<JSONObject, String> jsonKeyStream = jsonWithWmDS.keyBy(json -> json.getJSONObject("common").getString("uid"));

        //jsonKeyStream.print(">>>?>>>");


        jsonKeyStream.window(TumblingEventTimeWindows.of(Time.seconds(5))).process(new ProcessWindowFunction<JSONObject, String, String, TimeWindow>() {

            private ValueState<String> lastState;

            @Override
            public void open(Configuration parameters) throws Exception {
                lastState = getRuntimeContext().getState(new ValueStateDescriptor<String>("lastState", String.class));
            }

            //直接用的process 方法 等数据到齐触发窗口计算的时候会 走这个函数 iterable里面就是窗口的数据.
            @Override
            public void process(String s, ProcessWindowFunction<JSONObject, String, String, TimeWindow>.Context ctx, Iterable<JSONObject> iterable, Collector<String> out) throws Exception {

                //获取状态中的数据
                String date = lastState.value();

                //获取windows的时间
                long startTime = ctx.window().getStart();
                String today = DateFormatUtil.toDate(startTime);
                if (!today.equals(date)) { //数据中的日期和今日的窗口的日期不一样就证明是 今天的第一个数据

                    List<JSONObject> list = MyUtils.tolist(iterable);
                    //排序取出来最小的一条数据
                    JSONObject minJsonObject = Collections.min(list, (o1, o2) -> (int) (o2.getLong("ts") - o1.getLong("ts")));
                    out.collect(minJsonObject.toJSONString());

                    //更新状态
                    lastState.update(today);
                }
            }
        }).print(">>>>>");

        env.execute();
    }

}
