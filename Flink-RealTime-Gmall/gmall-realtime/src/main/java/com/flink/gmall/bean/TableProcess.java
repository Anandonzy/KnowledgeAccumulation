package com.flink.gmall.bean;

/**
 * @Author wangziyu1
 * @Date 2022/8/16 15:09
 * @Version 1.0
 */
import lombok.Data;

@Data
public class TableProcess {
    //来源表
    String sourceTable;
    //输出表
    String sinkTable;
    //输出字段
    String sinkColumns;
    //主键字段
    String sinkPk;
    //建表扩展
    String sinkExtend;
}
