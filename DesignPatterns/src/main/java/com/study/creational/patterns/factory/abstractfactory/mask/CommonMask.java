package com.study.creational.patterns.factory.abstractfactory.mask;

import com.study.creational.patterns.factory.abstractfactory.AbstractMask;

/**
 * @author wangziyu
 * @version 1.0
 * @since 2023/8/14 18:54
 */
public class CommonMask  extends AbstractMask {

    public CommonMask() {
        this.price = 1;
    }

    public void proteteMe() {
        System.out.println(" Commont 普通防护,及时保护!");
    }
}
