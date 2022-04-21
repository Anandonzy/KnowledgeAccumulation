package com.study.knowlages.juc.myvolatile;

class T1 {
    volatile int n = 0;

    public void add() {
        n++;
    }
}
