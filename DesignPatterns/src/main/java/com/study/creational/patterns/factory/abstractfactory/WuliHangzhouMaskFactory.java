package com.study.creational.patterns.factory.abstractfactory;

import com.study.creational.patterns.factory.abstractfactory.mask.CommonMask;

/**
 * @author wangziyu
 * @version 1.0
 * @since 2023/8/14 19:16
 * 分厂 制造口罩
 */
public class WuliHangzhouMaskFactory extends WuliMaskFactory{


    public AbstractMask newMask() {
        return new CommonMask();
    }
}
