package com.study.knowlages.juc.thread;

import lombok.Getter;

/**
 * @Author wangziyu1
 * @Date 2022/2/24 5:35 下午
 * @Version 1.0
 */
public enum CountryEnum {
    ONE(1, "齐"),
    TWO(2, "楚"),
    THREE(3, "燕"),
    FOUR(4, "赵"),
    FIVE(5, "魏"),
    SIX(6, "吴");

    @Getter
    private Integer retCode;
    @Getter
    private String retMessage;

    CountryEnum(Integer retCode, String retMessage) {
        this.retCode = retCode;
        this.retMessage = retMessage;
    }

    public static CountryEnum forEachCountry(Integer index) {
        CountryEnum[] values = CountryEnum.values();
        for (CountryEnum v : values) {
            if (index == v.getRetCode()) {
                return v;
            }
        }
        return null;
    }
}
