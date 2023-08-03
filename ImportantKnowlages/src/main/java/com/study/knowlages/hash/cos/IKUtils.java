/*
package com.study.knowlages.hash.cos;
*/
/**
 * @Author wangziyu1
 * @Date 2022/4/28 11:10
 * @Version 1.0
 *//*


import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

*/
/**
 * 分词相关工具类
 */
/*

public class IKUtils {

    */
/**
     * 以List的格式返回文本分词的结果
     * @param text
     * @return
     *//*

    public static List<String> divideText(String text){
        if(null == text || "".equals(text.trim())){
            return null;
        }
        List<String> resultList = new ArrayList<>();
        StringReader re = new StringReader(text);
        IKSegmenter ik = new IKSegmenter(re, true);
        Lexeme lex = null;
        try {
            while ((lex = ik.next()) != null) {
                resultList.add(lex.getLexemeText());
            }
        } catch (Exception e) {
            //TODO
        }
        return resultList;
    }

}
*/
