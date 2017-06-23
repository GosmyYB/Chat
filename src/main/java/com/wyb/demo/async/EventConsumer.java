package com.wyb.demo.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 事件消费者，负责从任务队列获取事件，交给相应的处理器处理
 * Created by wyb
 */
public class EventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    private static final EventConsumer instance = new EventConsumer();

    private BlockingQueue<EventModel> queue;

    private ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * 记录 EventType 的映射关系 EventHandler
     */
    private Map<EventType, List<EventHandler>> configMap = new HashMap<>();

    private EventConsumer() {
    }

    public static EventConsumer getInstance() {
        return instance;
    }

    public EventConsumer setQueue(BlockingQueue<EventModel> queue) {
        this.queue = queue;
        return this;
    }

    /**
     * 注册 EventHandler
     * @param handler
     * @return EventConsumer  为了使用链式编程
     */
    public EventConsumer register(EventHandler handler) {
        List<EventType> eventTypes =  handler.getSupportEventTypes();
        for (EventType eventType : eventTypes) {
            if (!configMap.containsKey(eventType)) {
                configMap.put(eventType, new ArrayList<>());
            }
            configMap.get(eventType).add(handler);
        }
        return this;
    }

    /**
     * 开启线程处理消息队列中的事件
     */
    public void consume() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        EventModel event = queue.take();
                        List<EventHandler> eventHandlers = configMap.get(event.getEventType());
                        for (EventHandler handler : eventHandlers) {
                            // 交给线程池处理
                            executorService.submit(new Task(event, handler));
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                }
            }
        });
        thread.start();
    }

    /**
     * 事件任务
     * 将 handler 和 event 封装成一个 task 以便用交给线程池处理
     */
    private class Task implements Runnable {
        private EventModel event;
        private EventHandler handler;

        public Task(EventModel event, EventHandler handler) {
            this.event = event;
            this.handler = handler;
        }

        @Override
        public void run() {
            handler.doHandler(event);
        }
    }
}
