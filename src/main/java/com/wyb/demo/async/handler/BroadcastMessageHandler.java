package com.wyb.demo.async.handler;

import com.wyb.demo.async.EventHandler;
import com.wyb.demo.async.EventModel;
import com.wyb.demo.async.EventType;
import com.wyb.demo.model.Client;
import com.wyb.demo.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 负责将消息广播给所有客户
 * Created by wyb
 */
public class BroadcastMessageHandler implements EventHandler {

    private static final Logger logger = LoggerFactory.getLogger(BroadcastMessageHandler.class);

    @Override
    public void doHandler(EventModel event) {
        broadcast(event);
        logger.info(Thread.currentThread().getName() + " has sent message");
    }

    /**
     * 广播消息
     *
     * @param event
     */
    private void broadcast(EventModel event) {
        Message message = event.getMessage();
        List<Client> clients = event.getClients();
        for (Client client : clients) {
            try {
                client.getOutput().writeObject(message);
            } catch (IOException e) {
                logger.error(e.getMessage());
                continue;
            }
        }
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.BROADCAST_MESSAGE);
    }
}
