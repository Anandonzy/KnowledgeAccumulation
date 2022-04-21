package com.flink.appAnalysis;

import com.flink.bean.MarketingUserBehavior;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * @version 1.0
 * @Author wangziyu1
 * @Date 9/18/21 4:04 PM
 * @注释
 */
public class Flink04_Project_AppAnalysis_By_Chanel {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();


        //1 APP市场推广统计 - 分渠道
   /*     env.addSource(new AppMarketingDataSource())
                .map(behavior -> Tuple2.of(behavior.getChannel() + "_" + behavior.getBehavior(), 1L))
                .returns(Types.TUPLE(Types.STRING, Types.LONG))
                .keyBy(f -> f.f0)
                .sum(1)
                .print();*/

        //2 APP市场推广统计 - 不分渠道
        env.addSource(new AppMarketingDataSource())
                .map(behavior -> Tuple2.of(behavior.getBehavior(), 1L))
                .returns(Types.TUPLE(Types.STRING, Types.LONG))
                .keyBy(f -> f.f0)
                .sum(1)
                .print();

        env.execute();

    }

    public static class AppMarketingDataSource extends RichSourceFunction<MarketingUserBehavior> {
        boolean canRun = true;
        Random random = new Random();
        List<String> channels = Arrays.asList("huawwei", "xiaomi", "apple", "baidu", "qq", "oppo", "vivo");
        List<String> behaviors = Arrays.asList("download", "install", "update", "uninstall");

        @Override

        public void run(SourceContext<MarketingUserBehavior> ctx) throws Exception {

            while (canRun) {
                MarketingUserBehavior marketingUserBehavior = new MarketingUserBehavior(
                        (long) random.nextInt(1000000),
                        behaviors.get(random.nextInt(behaviors.size())),
                        channels.get(random.nextInt(channels.size())),
                        System.currentTimeMillis());
                ctx.collect(marketingUserBehavior);
                Thread.sleep(2000);
            }

        }

        @Override
        public void cancel() {

            canRun = false;
        }
    }

}
