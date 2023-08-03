package com.flink.state;

import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeutils.base.StringSerializer;
import org.apache.flink.api.common.typeutils.base.VoidSerializer;
import org.apache.flink.api.scala.typeutils.Types;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.TwoPhaseCommitSinkFunction;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @Author wangziyu1
 * @Date 2022/12/6 13:42
 * @Version 1.0
 * 演示算子状态 了解即可
 * 并且演示 写文件 自己实现两阶段提交的过程 TwoPhaseCommitSinkFunction
 */
public class OperatorStateDemo {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);
        env.enableCheckpointing(10 * 1000L);

        env.addSource(new CountSouce())
                .addSink(new TreansactionalFileSink());


        env.execute();


    }

    private static class CountSouce extends RichSourceFunction<Long> implements CheckpointedFunction {

        private boolean isRunning = true;

        //保存偏移量
        private Long offset = 0L;


        // 声明一个算子状态变量
        // 列表状态变量中，只保存一个元素，保存刚消费完的偏移量
        // 算子状态不能使用ValueState
        private ListState<Long> state;

        //run 函数不是原子性的需要加锁 让其变成原子操作
        @Override
        public void run(SourceContext<Long> ctx) throws Exception {
            Object lock = ctx.getCheckpointLock();

            while (isRunning) {
                //加锁
                synchronized (lock) {
                    ctx.collect(offset);
                    offset += 1L;
                }
                Thread.sleep(1000L);
            }

        }

        @Override
        public void cancel() {
            isRunning = false;
        }

        // 当检查点分界线进入source算子时触发调用
        @Override
        public void snapshotState(FunctionSnapshotContext functionSnapshotContext) throws Exception {
            //保存刚消费完的偏移量
            state.clear();
            state.add(offset);
        }


        // 程序启动时触发调用（不管是第一次启动还是故障恢复）
        @Override
        public void initializeState(FunctionInitializationContext ctx) throws Exception {
            state = ctx.getOperatorStateStore().getListState(new ListStateDescriptor<Long>("state", Types.LONG()));


            //如果state 不为空 说明程序是从故障中护肤之后启动的, 所以里面的元素就是刚消费完的偏移量
            //如果程序是第一次启动,循环不执行
            //如果 是故障恢复 那么循环执行一次
            for (Long l : state.get()) {
                offset = l;
            }
        }
    }

    /**
     * 自己实现两阶段提交代码
     * 开启事务: 创建临时文件
     * 预提交: 将数据输出到临时文件
     * 正式提交:将临时文件名改成正式文件名
     * 回滚:删除临时文件
     */
    private static class TreansactionalFileSink extends TwoPhaseCommitSinkFunction<Long, String, Void> {
        private BufferedWriter transactionWriter;

        public TreansactionalFileSink() {
            //初始化父类
            super(StringSerializer.INSTANCE, VoidSerializer.INSTANCE);
        }

        @Override
        protected void invoke(String fileName, Long in, Context ctx) throws Exception {
            transactionWriter.write(in + "\n"); //将数据写入缓冲区

        }

        //每个检查点对应一个事务
        @Override
        protected String beginTransaction() throws Exception {
            long timeNow = System.currentTimeMillis();
            int taskIdx = getRuntimeContext().getIndexOfThisSubtask();
            String fileName = timeNow + "-" + taskIdx;
            //创建临时文件
            Path tempFilePath = Paths.get("/Users/wangziyu/Downloads/filetemp/" + fileName);
            Files.createFile(tempFilePath);
            //将缓冲区指定为 临时文件的写入缓冲区
            this.transactionWriter = Files.newBufferedWriter(tempFilePath);
            return fileName;
        }

        //预提交的逻辑
        @Override
        protected void preCommit(String s) throws Exception {
            transactionWriter.flush(); //将数据写入到临时文件里面
            transactionWriter.close();
        }

        //正式提交
        @Override
        protected void commit(String fileName) {
            Path tempFilePath = Paths.get("/Users/wangziyu/Downloads/filetemp/" + fileName);
            if (Files.exists(tempFilePath)) { //如果存在临时文件

                try {
                    //将临时文件名字改为正式文件名字
                    Path commitFilePath = Paths.get("/Users/wangziyu/Downloads/filetarget/" + fileName);
                    Files.move(tempFilePath, commitFilePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //回滚逻辑
        @Override
        protected void abort(String fileName) {
            Path tempPath = Paths.get("/Users/wangziyu/Downloads/filetemp/" + fileName);

            if (Files.exists(tempPath)) {
                try {
                    //删除临时文件
                    Files.delete(tempPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }
    }
}
