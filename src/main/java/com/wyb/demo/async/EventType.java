package com.wyb.demo.async;

/**
 * 事件类型
 * Created by wyb
 */
public enum EventType {

    /**
     * 广播消息
     */
    BROADCAST_MESSAGE(0);

    /**
     * 枚举对象包含的值
     */
    private int value;

    private EventType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
