package com.wyb.demo.async.handler;

import com.wyb.demo.async.EventHandler;
import com.wyb.demo.async.EventModel;
import com.wyb.demo.async.EventType;
import com.wyb.demo.model.Client;
import com.wyb.demo.model.Message;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by wyb
 */
public class SendMessageHandler implements EventHandler {

    @Override
    public void doHandler(EventModel event) {
        sendMessage(event);
        System.out.println(Thread.currentThread().getName() + " has sent message");
    }

    /**
     * 发送消息
     *
     * @param event
     */
    private void sendMessage(EventModel event) {
        Message message = event.getMessage();
        List<Client> clients = event.getClients();
        for (Client client : clients) {
            try {
                client.getOutput().writeObject(message);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.SEND_MESSAGE);
    }
}
