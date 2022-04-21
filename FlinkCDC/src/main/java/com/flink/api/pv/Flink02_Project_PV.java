package com.flink.api.pv;


import com.flink.bean.UserBehavior;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

/**
 * @version 1.0
 * @Author wangziyu1
 * @Date 9/18/21 3:30 PM
 * @注释
 */
public class Flink02_Project_PV {


    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.readTextFile("/Users/wangziyu/Desktop/UserBehavior.csv")
                .map(line -> {
                    String[] split = line.split(",");
                    return new UserBehavior(Long.valueOf(split[0]),
                            Long.valueOf(split[1]),
                            Integer.valueOf(split[2]),
                            split[3],
                            Long.valueOf(split[4]));
                })
                .filter(behavior -> "pv".equals(behavior.getBehavior()))
                .keyBy(UserBehavior::getBehavior)
                .process(new KeyedProcessFunction<String, UserBehavior, Long>() {
                    long count = 0;

                    @Override
                    public void processElement(UserBehavior userBehavior, Context context, Collector<Long> collector) throws Exception {
                        count++;
                        collector.collect(count);
                    }
                }).print();

        env.execute();

    }
}
