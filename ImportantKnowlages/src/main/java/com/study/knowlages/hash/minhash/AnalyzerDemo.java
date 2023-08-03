package com.study.knowlages.hash.minhash;

/**
 * @Author wangziyu1
 * @Date 2022/4/28 20:16
 * @Version 1.0
 */
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;

public class AnalyzerDemo {
    public static void main(String[] args) throws IOException {
        WhitespaceAnalyzer analyzer = new WhitespaceAnalyzer();

        //String text = "The Lucene PMC is pleased to announce the release of the Apache Solr Reference Guide for Solr 4.4.";
        String text = "新冠疫苗今天效果不错";

        TokenStream tokenStream = analyzer.tokenStream("field", text);
        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();
        while (tokenStream.incrementToken()) {
            System.out.println(charTermAttribute.toString());
        }
        tokenStream.end();
        tokenStream.close();
    }
}