package com.wyb.demo.async;

import com.wyb.demo.model.Client;
import com.wyb.demo.model.Message;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 封装事件
 * Created by wyb
 */
public class EventModel {

    /**
     * 事件类型
     */
    private EventType eventType;

    /**
     * 接收消息的客户列表
     */
    private List<Client> clients;

    /**
     * 消息
     */
    private Message message;

    /**
     * 扩展字段
     */
    private Map<String, Object> extents = new ConcurrentHashMap<>();

    public EventType getEventType() {
        return eventType;
    }

    public EventModel setEventType(EventType eventType) {
        this.eventType = eventType;
        return this;
    }

    public List<Client> getClients() {
        return clients;
    }

    public EventModel setClients(List<Client> clients) {
        this.clients = clients;
        return this;
    }

    public Message getMessage() {
        return message;
    }

    public EventModel setMessage(Message message) {
        this.message = message;
        return this;
    }

    public void add(String key, Object value) {
        extents.put(key, value);
    }

    public Object get(String key) {
        return extents.get(key);
    }
}
