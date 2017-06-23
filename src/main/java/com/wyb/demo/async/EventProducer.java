package com.wyb.demo.async;

import com.wyb.demo.model.Message;

import java.util.concurrent.BlockingQueue;

/**
 * Created by wyb
 */
public class EventProducer {

    private static final EventProducer instance = new EventProducer();

    private BlockingQueue<EventModel> queue;

    private EventProducer() {
    }

    public static EventProducer getInstance() {
        return instance;
    }

    public void setQueue(BlockingQueue<EventModel> queue) {
        this.queue = queue;
    }

    /**
     * 发送事件到消息队列
     * @param event
     */
    public void fireEvent(EventModel event) {
        try {
            queue.put(event);
        } catch (Exception e) {
            e.getMessage();
        }
    }
}
