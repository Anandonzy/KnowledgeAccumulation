package com.flink.ads;

import com.flink.bean.OrderEvent;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.time.Duration;

/**
 * @version 1.0
 * @Author wangziyu1
 * @Date 10/18/21 5:02 PM
 * @注释
 * 在电商网站中，订单的支付作为直接与营销收入挂钩的一环，在业务流程中非常重要。
 *      对于订单而言，为了正确控制业务流程，也为了增加用户的支付意愿，网站一般会设置一个支付失效时间，超过一段时间不支付的订单就会被取消。
 *      另外，对于订单的支付，我们还应保证用户支付的正确性，这可以通过第三方支付平台的交易数据来做一个实时对账。
 */
public class Flink08_Project_OrderWatch {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);
        // 创建WatermarkStrategy
        WatermarkStrategy<OrderEvent> wms = WatermarkStrategy
                .<OrderEvent>forBoundedOutOfOrderness(Duration.ofSeconds(20))
                .withTimestampAssigner(new SerializableTimestampAssigner<OrderEvent>() {
                    @Override
                    public long extractTimestamp(OrderEvent element, long recordTimestamp) {
                        return element.getEventTime();
                    }
                });
        env
                .readTextFile("/Users/wangziyu/Desktop/data/OrderLog.csv")
                .map(line -> {
                    String[] datas = line.split(",");
                    return new OrderEvent(
                            Long.valueOf(datas[0]),
                            datas[1],
                            datas[2],
                            Long.parseLong(datas[3]) * 1000);

                })
                .assignTimestampsAndWatermarks(wms)
                .keyBy(OrderEvent::getOrderId)
                .process(new KeyedProcessFunction<Long, OrderEvent, String>() {
                    private ValueState<Long> timeoutTs;
                    private ValueState<OrderEvent> createEvent;
                    private ValueState<OrderEvent> payEvent;

                    @Override
                    public void open(Configuration parameters) throws Exception {
                        timeoutTs = getRuntimeContext().getState(new ValueStateDescriptor<Long>("timeoutTs", Long.class));
                        createEvent = getRuntimeContext().getState(new ValueStateDescriptor<OrderEvent>("createEvent", OrderEvent.class));
                        payEvent = getRuntimeContext().getState(new ValueStateDescriptor<OrderEvent>("payEvent", OrderEvent.class));
                    }

                    @Override
                    public void processElement(OrderEvent value, Context ctx, Collector<String> out) throws Exception {

                        //判断数据类型 ,下单还是支付
                        String eventType = value.getEventType();


                        // a: 有支付又有订单的情况
                        if ("create".equals(eventType)) {
                            //判断支付是否已经来了
                            if (payEvent.value() == null) {
                                //支付还没来 ,把自己保存起来
                                createEvent.update(value);
                            } else {
                                //支付来过,判断创建订单的时间和支付的时间间隔是否小于15min
                                if (payEvent.value().getEventTime() - value.getEventTime() <= 15 * 60 * 1000) {
                                    out.collect("订单: " + value.getOrderId() + " 正常支付");
                                } else {
                                    out.collect("订单: " + value.getOrderId() + " 在超时时间完成的支付, 系统可能存在漏洞");
                                }

                            }
                            payEvent.clear();

                        } else {
                            //判断下单是否已经来了
                            if (createEvent.value() == null) {
                                payEvent.update(value);

                            } else {
                                if (value.getEventTime() - createEvent.value().getEventTime() <= 15 * 60 * 1000) {
                                    out.collect("订单: " + value.getOrderId() + " 正常支付");
                                } else {
                                    out.collect("订单: " + value.getOrderId() + " 在超时时间完成的支付, 系统可能存在漏洞");
                                }
                                createEvent.clear();


                            }
                        }

                        // b: 只有支付或者只有订单的情况  如果超过20分钟,没有收到另外一方的信息
                        // 使用定时器来处理这种异常情况
                        if (timeoutTs.value() == null) {
                            //第一条数据过来了
                            ctx.timerService().registerEventTimeTimer(value.getEventTime() + 20 * 60 * 1000L);
                            timeoutTs.update(value.getEventTime() + 20 * 60 * 1000L);

                            //第二条数据来了,删除定时器
                        } else {
                            timeoutTs.clear();
                        }
                    }


                    @Override
                    public void onTimer(long timestamp, OnTimerContext ctx, Collector<String> out) throws Exception {
                        if (payEvent.value() == null) {
                            // 说明payEvent没来
                            out.collect("订单: " + ctx.getCurrentKey() + " 支付超时, 订单自动取消!");
                        } else {
                            //说明createEvent没来
                            out.collect("订单: " + ctx.getCurrentKey() + " 成功支付, 但是没有下单数据, 请检查系统!");
                        }
                        payEvent.clear();
                        createEvent.clear();
                        timeoutTs.clear();
                    }

                }).print();
        env.execute();

    }
}
