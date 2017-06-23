package com.wyb.demo.async;

import java.util.List;

/**
 * Created by wyb
 */
public interface EventHandler {

    /**
     * 处理事件
     * @param event 要处理的事件对象
     */
    void doHandler(EventModel event);


    /**
     * 支持处理的事件
     * @return
     */
    List<EventType> getSupportEventTypes();

}
