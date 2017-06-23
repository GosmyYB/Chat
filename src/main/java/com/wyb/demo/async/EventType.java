package com.wyb.demo.async;

/**
 * Created by wyb
 */
public enum EventType {

    SEND_MESSAGE(0); // 构造了一个枚举对象

    private int value;

    private EventType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
