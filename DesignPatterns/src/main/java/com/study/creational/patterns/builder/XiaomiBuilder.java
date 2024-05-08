package com.study.creational.patterns.builder;

/**
 * @author wangziyu
 * @version 1.0
 * @since 2023/8/18 17:08
 */
public class XiaomiBuilder  extends  AbstractBuilder{


    public XiaomiBuilder() {
//        this.phone = new Phone();
        phone = Phone.builder().build();
    }

    public AbstractBuilder customCpu(String cpu) {
        phone.cpu = cpu;
        return this;
    }

    public AbstractBuilder customMem(String mem) {
        phone.memory = mem;
        return this;

    }

    public AbstractBuilder customDisk(String disk) {
        phone.disk = disk;
        return this;
    }

    public AbstractBuilder customCam(String cam) {
        phone.camera = cam;
        return this;
    }

}
