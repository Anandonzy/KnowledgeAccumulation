package com.study.knowlages.singleton;

import java.io.IOException;
import java.util.Properties;

public class SingletonHungryStatic {

    public static SingletonHungryStatic INSTANCE;
    private String info;

    static {
        try {
            Properties properties = new Properties();
            properties.load(SingletonHungryStatic.class.getClassLoader().getResourceAsStream("singlton.properties"));
            INSTANCE = new SingletonHungryStatic(properties.getProperty("info"));
        } catch (IOException e) {
            throw new RuntimeException();
        }

    }

    private SingletonHungryStatic(String info) {
        this.info = info;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    @Override
    public String toString() {
        return "SingletonHungryStatic{" +
                "info='" + info + '\'' +
                '}';
    }

    public static void main(String[] args) {
        SingletonHungryStatic instance = SingletonHungryStatic.INSTANCE;
        System.out.println(instance.toString());
    }
}
