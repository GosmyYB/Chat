package com.wyb.demo.async;

import java.util.List;

/**
 * 事件处理器
 * Created by wyb
 */
public interface EventHandler {

    /**
     * 处理事件
     * @param event 要处理的事件对象
     */
    void doHandler(EventModel event);


    /**
     * 返回该处理器可处理的事件
     * @return 事件类型列表
     */
    List<EventType> getSupportEventTypes();

}
