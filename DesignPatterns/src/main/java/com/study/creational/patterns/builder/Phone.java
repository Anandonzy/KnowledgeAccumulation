package com.study.creational.patterns.builder;

import lombok.Builder;

/**
 * @author wangziyu
 * @version 1.0
 * @since 2023/8/18 17:04
 */
@Builder
public class Phone {
    protected String cpu;
    protected String memory;
    protected String disk;
    protected String camera;

    public String getCpu() {
        return cpu;
    }

    public String getMemory() {
        return memory;
    }

    public String getDisk() {
        return disk;
    }

    public String getCamera() {
        return camera;
    }

    @Override
    public String toString() {
        return "Phone{" +
                "cpu='" + cpu + '\'' +
                ", memory='" + memory + '\'' +
                ", disk='" + disk + '\'' +
                ", camera='" + camera + '\'' +
                '}';
    }
}
