package com.flink.order;


import com.flink.bean.OrderEvent;
import com.flink.bean.TxEvent;
import org.apache.flink.streaming.api.datastream.ConnectedStreams;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.util.Collector;

import java.util.HashMap;
import java.util.Map;

/**
 * @version 1.0
 * @Author wangziyu1
 * @Date 9/18/21 5:29 PM
 * @注释
 */
public class Flink06_Project_Order {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);


        //1.读取订单信息 34729	pay	sd76f87d6	1558430844
        SingleOutputStreamOperator<OrderEvent> orderEventDS = env.readTextFile("/Users/wangziyu/Desktop/data/OrderLog.csv")
                .map(line -> {
                    String[] datas = line.split(",");
                    return new OrderEvent(
                            Long.valueOf(datas[0]),
                            datas[1],
                            datas[2],
                            Long.valueOf(datas[3]));

                });


        //2.读取交易信息  sd76f87d6	wechat	1558430847
        SingleOutputStreamOperator<TxEvent> txDS = env.readTextFile("/Users/wangziyu/Desktop/data/ReceiptLog.csv")
                .map(line -> {
                    String[] datas = line.split(",");
                    return new TxEvent(datas[0], datas[1], Long.valueOf(datas[2]));
                });


        //3.connect 两个流的信息
        ConnectedStreams<OrderEvent, TxEvent> orderAndTx = orderEventDS.connect(txDS);

        //4. 因为不同的数据流到达的先后顺序不一致，所以需要匹配对账信息.  输出表示对账成功与否
        orderAndTx
                .keyBy("txId", "txId")
                .process(new CoProcessFunction<OrderEvent, TxEvent, String>() {

                    Map<String, OrderEvent> orderMap = new HashMap<>();
                    Map<String, TxEvent> txMap = new HashMap<>();

                    @Override
                    public void processElement1(OrderEvent value, Context context, Collector<String> collector) throws Exception {

                        //获取交易信息
                        if(txMap.containsKey(value.getTxId())){
                            collector.collect("订单" + value + "对账成功");
                            txMap.remove(value.getTxId());

                        }else{
                            orderMap.put(value.getTxId(), value);
                        }

                    }

                    @Override
                    public void processElement2(TxEvent value, Context context, Collector<String> collector) throws Exception {
                        // 获取订单信息
                        if (orderMap.containsKey(value.getTxId())) {
                            OrderEvent orderEvent = orderMap.get(value.getTxId());
                            collector.collect("订单: " + orderEvent + " 对账成功");
                            orderMap.remove(value.getTxId());
                        } else {
                            txMap.put(value.getTxId(), value);
                        }
                    }
                })
                .print();
        env.execute();

    }
}
