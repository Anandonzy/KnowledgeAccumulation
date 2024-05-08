package com.study.creational.patterns.factory.abstractfactory;

import com.study.creational.patterns.factory.abstractfactory.mask.N95Mask;

/**
 * @author wangziyu
 * @version 1.0
 * @since 2023/8/14 19:07
 */
public class WuliWuhanMaskFactory extends WuliMaskFactory {
    public AbstractMask newMask() {
        return new N95Mask();
    }
}
