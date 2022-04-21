package com.study.knowlages.juc.tl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @Author wangziyu1
 * @Date 2022/4/12 17:22
 * @Version 1.0
 * SimpleDateFormat多线程访问的时候会出现内存泄漏的问题
 * 案例演示
 */
public class ThreadLocalDateUtils {

    /**
     * d传统给的方法 案例演示
     */
    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 使用ThreadLocal 演示
     * 初始化
     */
    public static final ThreadLocal<SimpleDateFormat> sdfThreadLocal = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    /**
     * 使用ThreadLocal 解析
     *
     * @param stringDate
     * @return
     * @throws ParseException
     */
    public static Date threadLocalParse(String stringDate) throws ParseException {
        return sdfThreadLocal.get().parse(stringDate);
    }

    /**
     * 第一种方法:加上synchronized肯定可以 但是多线程肯定不能那么用
     *
     * @param stringDate
     * @return
     * @throws ParseException
     */
    public static Date parse(String stringDate) throws ParseException {
        return sdf.parse(stringDate);
    }

    /**
     * 第三种方法: DateTimeFormatter 代替 SimpleDateFormat
     * 如果是jdk1.8 就可以使用 Instant 代替Date,LocalDateTime 代替Calendar
     * DateTimeFormat 代替SimpleDateFormat 官方给的解释simple beautiful strong immutable
     * thread-safe。
     */
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String format(LocalDateTime localDateTime) {
        return DATE_TIME_FORMATTER.format(localDateTime);
    }

    public static LocalDateTime parse2(String dateStrings) {
        return LocalDateTime.parse(dateStrings, DATE_TIME_FORMATTER);
    }

    public static void main(String[] args) {

        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    /**
                     * 第二种方法 每个线程new一个.
                     */
                    //System.out.println(ThreadLocalDateUtils.parse("2011-11-11 11:11:11"));

                    /*SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    System.out.println(sdf.parse("2011-11-11 11:11:11"));
                    sdf = null;*/
                    System.out.println(ThreadLocalDateUtils.threadLocalParse("2011-11-11 11:11:11"));
                } catch (ParseException e) {
                    e.printStackTrace();
                }finally {
                    //必须要remove
                    ThreadLocalDateUtils.sdfThreadLocal.remove();
                }
            }, String.valueOf(i)).start();
        }

    }
}
