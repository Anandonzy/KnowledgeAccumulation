package com.study.creational.patterns.factory.abstractfactory.mask;

import com.study.creational.patterns.factory.abstractfactory.AbstractMask;

/**
 * @author wangziyu
 * @version 1.0
 * @since 2023/8/14 18:52
 */
public class N95Mask  extends AbstractMask {

    public N95Mask() {
        this.price = 2;
    }

    public void proteteMe() {
        System.out.println("N95 mask ...超级防护!");

    }
}
