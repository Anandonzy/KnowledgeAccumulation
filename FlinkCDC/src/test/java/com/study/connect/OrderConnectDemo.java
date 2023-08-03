package com.study.connect;

import com.flink.bean.OrderEvent;
import com.flink.bean.TxEvent;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.table.planner.delegation.StreamExecutor;
import org.apache.flink.util.Collector;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author wangziyu1
 * @Date 2023/2/6 17:33
 * @Version 1.0
 */
public class OrderConnectDemo {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);

        SingleOutputStreamOperator<OrderEvent> orderDS = env.readTextFile("/Users/wangziyu/Desktop/data/OrderLog.csv")
                .map(line -> {
                    String[] datas = line.split(",");
                    return new OrderEvent(
                            Long.valueOf(datas[0]),
                            datas[1],
                            datas[2],
                            Long.parseLong(datas[3]) * 1000);

                });

        SingleOutputStreamOperator<TxEvent> txDS = env
                .readTextFile("/Users/wangziyu/Desktop/data/ReceiptLog.csv")
                .map(new MapFunction<String, TxEvent>() {
                    @Override
                    public TxEvent map(String value) throws Exception {
                        String[] datas = value.split(",");
                        return new TxEvent(
                                datas[0],
                                datas[1],
                                Long.valueOf(datas[2])
                        );
                    }
                });

        orderDS.connect(txDS)
                .keyBy(order -> order.getOrderId(), tx -> tx.getTxId())

                //4)	因为不同的数据流到达的先后顺序不一致，所以需要匹配对账信息
                .process(new CoProcessFunction<OrderEvent, TxEvent, String>() {
                    private Map<String, OrderEvent> orderMap = new HashMap();
                    private Map<String, TxEvent> txMap = new HashMap();

                    @Override
                    public void processElement1(OrderEvent value, Context ctx, Collector<String> out) throws Exception {
                        TxEvent txEvent = txMap.get(value.getTxId());

                        if (txEvent == null) {
                            orderMap.put(value.getTxId(), value);
                        } else {
                            out.collect("订单[" + value.getOrderId() + "]对账成功");
                            txMap.remove(value.getTxId());
                        }


                    }

                    @Override
                    public void processElement2(TxEvent value, Context ctx, Collector<String> out) throws Exception {
                        OrderEvent orderEvent = orderMap.get(value.getTxId());
                        if (orderEvent == null) {
                            txMap.put(value.getTxId(), value);
                        } else {
                            out.collect("订单[" + orderEvent.getOrderId() + "]对账成功");
                            orderMap.remove(value.getTxId());
                        }

                    }
                }).print();


        env.execute();


    }
}
