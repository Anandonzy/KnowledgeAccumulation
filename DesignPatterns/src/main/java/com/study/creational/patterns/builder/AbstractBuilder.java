package com.study.creational.patterns.builder;

/**
 * @author wangziyu
 * @version 1.0
 * @since 2023/8/18 17:07
 * 规定订制那些属性
 */
public abstract class AbstractBuilder {
    Phone phone;
    /**
     * 规定定制cpu
     * @param cpu
     */
    public abstract AbstractBuilder customCpu(String cpu);
    public abstract AbstractBuilder customMem(String mem);
    public abstract AbstractBuilder customDisk(String disk);
    public abstract AbstractBuilder customCam(String cam);
    public Phone getProduct(){
        return phone;
    }
}
