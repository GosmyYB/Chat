package com.wyb.demo.model;

import java.io.Serializable;

/**
 * 消息体
 * Created by wyb.
 */
public class Message implements Serializable {

    /**
     * 发出消息用户的用户名
     */
    private String from;

    /**
     * 消息内容
     */
    private String body;

    public Message(String from, String body) {
        this.from = from;
        this.body = body;
    }

    public String getFrom() {
        return from;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "Message{" +
                "from='" + from + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}
