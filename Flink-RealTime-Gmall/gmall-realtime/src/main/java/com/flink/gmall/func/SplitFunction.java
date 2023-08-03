package com.flink.gmall.func;

import com.flink.gmall.utils.KeywordUtil;
import org.apache.flink.table.annotation.DataTypeHint;
import org.apache.flink.table.annotation.FunctionHint;
import org.apache.flink.table.functions.TableFunction;
import org.apache.flink.types.Row;

import java.io.IOException;
import java.util.List;

/**
 * @Author wangziyu1
 * @Date 2022/8/29 10:02
 * @Version 1.0
 */
@FunctionHint(output = @DataTypeHint("ROW<word STRING>"))
public class SplitFunction extends TableFunction<Row> {

    public void eval(String str) {
//        for (String s : str.split(" ")) {
//            collect(Row.of(s, s.length()));
//        }

        List<String> list = null;
        try {
            list = KeywordUtil.splitKeyword(str);
            for (String word : list) {
                collect(Row.of(word));
            }
        } catch (IOException e) {
            collect(Row.of(str));
        }
    }
}
