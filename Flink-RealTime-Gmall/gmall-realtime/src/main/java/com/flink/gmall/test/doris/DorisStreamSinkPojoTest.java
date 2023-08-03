package com.flink.gmall.test.doris;

import com.alibaba.fastjson.JSON;
import lombok.*;
import org.apache.doris.flink.cfg.DorisExecutionOptions;
import org.apache.doris.flink.cfg.DorisOptions;
import org.apache.doris.flink.cfg.DorisSink;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Properties;

/**
 * @Author wangziyu1
 * @Date 2022/9/19 14:32
 * @Version 1.0
 */
public class DorisStreamSinkPojoTest {


    public static void main(String[] args) throws Exception {

        //TODO 1.获取执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1); //生产环境中设置为Kafka主题的分区数
        Properties pro = new Properties();
        pro.setProperty("format", "json");
        pro.setProperty("strip_outer_array", "true");
        env.fromElements(
                        new Site(1024, (short) 101, "google", 101))
                .map(JSON::toJSONString)
                .addSink(DorisSink.sink(
                        //执行参数
                        new DorisExecutionOptions.Builder()
                                .setBatchIntervalMs(2000L)
                                .setBatchSize(1024 * 1024)
                                .setEnableDelete(false)
                                .setMaxRetries(3)
                                .setStreamLoadProp(pro)
                                .build(),
                        //连接参数
                        new DorisOptions.Builder()
                                .setFenodes("192.168.15.205:7030")
                                .setUsername("root")
                                .setPassword("aaaaaa")
                                .setTableIdentifier("test_db.table1")
                                .build())
                );


        env.execute();

    }
}

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
class Site {

    private Integer siteid;
    private Short citycode;
    private String username;
    private Integer pv;
}