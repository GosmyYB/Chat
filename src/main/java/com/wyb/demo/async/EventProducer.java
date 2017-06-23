package com.wyb.demo.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

/**
 * 事件生产者，负责将事件提交到事件队列。
 * Created by wyb
 */
public class EventProducer {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

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
     * 将事件提交到消息队列
     * @param event
     */
    public void fireEvent(EventModel event) {
        try {
            queue.put(event);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
