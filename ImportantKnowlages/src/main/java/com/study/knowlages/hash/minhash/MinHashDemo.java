package com.study.knowlages.hash.minhash;

/**
 * @Author wangziyu1
 * @Date 2022/4/25 14:56
 * @Version 1.0
 */

import lombok.SneakyThrows;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.codelibs.minhash.MinHash;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * compare 方法返回文本之间的相似性。值从 0 到 1，但是值小于 0.5 表示不同的文本。
 */
public class MinHashDemo {
    public static void main(String[] args) throws IOException {
        //String text = "Fess is very powerful and easily deployable Enterprise Search Server.";
        String text = "降价72万，情怀门槛更低，红旗H9";
        byte[] minhash = calculateMinHash(text);

        //String text1 = "Fess is very powerful and easily deployable Search Server.";
        String text1 = "降价72万，情怀门槛更低，红旗H9";
        byte[] minhash1 = calculateMinHash(text1);
        float score1 = MinHash.compare(minhash, minhash1);
        System.out.println(score1);

        System.out.println(MinHash.toBinaryString(minhash));
        System.out.println(MinHash.toBinaryString(minhash1));


        // Compare a different text.
        String text2 = "Solr is the popular, blazing fast open source enterprise search platform";
        //byte[] minhash2 = calculateMinHash(text2);
        //float score2 = MinHash.compare(minhash, minhash2);
        //System.out.println(score2); // 0.953125
        //
        //System.out.println("--------");
        //System.out.println(MinHash.toBinaryString(minhash));
        //System.out.println(MinHash.toBinaryString(minhash1));
        ////float score3 = MinHash.compare(MinHash.toBinaryString(minhash), MinHash.toBinaryString(minhash1));
    }

    @SneakyThrows
    private static byte[] calculateMinHash(String text) {
        // Lucene's tokenizer parses a text.
        Tokenizer tokenizer = new WhitespaceTokenizer();
        // The number of bits for each hash value.
        int hashBit = 1;
        // A base seed for hash functions.
        int seed = 0;
        // The number of hash functions.
        int num = 64;
        // Analyzer for 1-bit 32 hash with custom Tokenizer.
        Analyzer analyzer = MinHash.createAnalyzer(tokenizer, hashBit, seed, num);

        // Calculate a minhash value. The size is hashBit*num.
        //return MinHash.calculate(analyzer, text);
        return MinHash.calculate(analyzer, filterChinesePunctuation(text));
    }

    public static String filterChinesePunctuation(String str) {
        Pattern p = Pattern.compile("\\pP|\\pS|\\pC|\\pN|\\pZ");
        Matcher matcher = p.matcher(str);
        return matcher.replaceAll("").trim();
    }

}