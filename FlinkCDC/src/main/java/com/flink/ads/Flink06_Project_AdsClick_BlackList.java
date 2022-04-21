package com.flink.ads;

import com.flink.bean.AdsClickLog;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.time.*;


/**
 * @version 1.0
 * @Author wangziyu1
 * @Date 10/18/21 11:15 AM
 * @注释: 我们进行的点击量统计，同一用户的重复点击是会叠加计算的。
 * 在实际场景中，同一用户确实可能反复点开同一个广告，这也说明了用户对广告更大的兴趣；
 * 但是如果用户在一段时间非常频繁地点击广告，这显然不是一个正常行为，有刷点击量的嫌疑。
 * 所以我们可以对一段时间内（比如一天内）的用户点击行为进行约束，如果对同一个广告点击超过一定限额（比如100次）
 * ，应该把该用户加入黑名单并报警，此后其点击行为不应该再统计。
 * 两个功能:
 * 1. 告警: 使用侧输出流
 * 2. 已经进入黑名单的用户的广告点击记录不再进行统计
 */
public class Flink06_Project_AdsClick_BlackList {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);
        // 创建WatermarkStrategy
        WatermarkStrategy<AdsClickLog> wms = WatermarkStrategy
                .<AdsClickLog>forBoundedOutOfOrderness(Duration.ofSeconds(20))
                .withTimestampAssigner(new SerializableTimestampAssigner<AdsClickLog>() {
                    @Override
                    public long extractTimestamp(AdsClickLog element, long recordTimestamp) {
                        return element.getTimestamp() * 1000L;
                    }
                });
        SingleOutputStreamOperator<String> result = env
                .readTextFile("/Users/wangziyu/Desktop/data/AdClickLog.csv")
                .map(line -> {
                    String[] datas = line.split(",");
                    return new AdsClickLog(Long.valueOf(datas[0]),
                            Long.valueOf(datas[1]),
                            datas[2],
                            datas[3],
                            Long.valueOf(datas[4]));
                })
                .assignTimestampsAndWatermarks(wms)
                // 按照装 (用户, 广告) 分组
                .keyBy(new KeySelector<AdsClickLog, Tuple2<Long, Long>>() {
                    @Override
                    public Tuple2<Long, Long> getKey(AdsClickLog log) throws Exception {
                        return Tuple2.of(log.getUserId(), log.getAdId());
                    }
                })
                // 1. 使用process 实现黑名单过滤
                .process(new KeyedProcessFunction<Tuple2<Long, Long>, AdsClickLog, String>() {
                    private ValueState<Boolean> warned;
                    private ValueState<Long> clickCount;

                    @Override
                    public void open(Configuration parameters) throws Exception {
                        clickCount = getRuntimeContext().getState(new ValueStateDescriptor<Long>("clickCount", Long.class));
                        warned = getRuntimeContext().getState(new ValueStateDescriptor<Boolean>("warned", Boolean.class));

                    }


                    //统计计数
                    @Override
                    public void processElement(AdsClickLog ele, Context ctx, Collector<String> out) throws Exception {
                        // 1. 统计次数

                        if (clickCount.value() == null) { //如果是第一条数据则更新成1
                            //每天的第一天数据注册为定时器,明天0:0:0 出发这个定时器
                            Long now = ctx.timestamp();
                            LocalDate today = LocalDateTime.ofEpochSecond(now
                                    / 1000, 0, ZoneOffset.ofHours(8)).toLocalDate();
                            long tomorrow = LocalDateTime.of(today.plusDays(1), LocalTime.of(0, 0, 0)).toEpochSecond(ZoneOffset.ofHours(8));
                            ctx.timerService().registerEventTimeTimer(tomorrow);
                            clickCount.update(1L);
                            out.collect("用户: " + ele.getUserId() + ",广告: " + ele.getAdId() + ",点击量: " + clickCount.value());

                        } else if (clickCount.value() < 99) {
                            clickCount.update(clickCount.value() + 1L);
                            out.collect("用户: " + ele.getUserId() + ",广告: " + ele.getAdId() + ",点击量: " + clickCount.value());
                        } else { //产生告警信息
                            if (warned.value() == null) {
                                String msg = "用户: " + ele.getUserId()
                                        + "对广告: " + ele.getAdId()
                                        + "的点击量是: " + (clickCount.value() + 1L);
                                ctx.output(new OutputTag<String>("黑名单") {
                                }, msg);
                                warned.update(true);

                            }
                        }

                    }

                    @Override
                    public void onTimer(long timestamp, OnTimerContext ctx, Collector<String> out) throws Exception {
                        // 黑名单相关数据是应该每天一清零: 使用定时器, 在每天的0:0:0清零数据
                        warned.clear();
                        clickCount.clear();

                    }
                });
        result.print();
        result.getSideOutput(new OutputTag<String>("Black List") {
        }).print("Black List");
        env.execute();


    }
}
