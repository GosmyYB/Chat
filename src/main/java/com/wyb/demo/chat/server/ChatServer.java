package com.wyb.demo.chat.server;

import com.wyb.demo.async.EventConsumer;
import com.wyb.demo.async.EventModel;
import com.wyb.demo.async.EventProducer;
import com.wyb.demo.async.EventType;
import com.wyb.demo.async.handler.SendMessageHandler;
import com.wyb.demo.model.Client;
import com.wyb.demo.model.Message;

import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by wyb.
 */
public class ChatServer {

    // 利用 CopyOnWriteArrayList 解决List 的同步问题
    private List<Client> clients = new CopyOnWriteArrayList<>();

    /**
     * 线程池 为多个客户端提供线程服务
     */
    private ExecutorService pool = Executors.newCachedThreadPool();

    private ScheduledExecutorService autoClearService = Executors.newSingleThreadScheduledExecutor();

    private boolean isRunning;

    public static void main(String[] args) throws IOException {
        BlockingQueue<EventModel> queue = new LinkedBlockingDeque<>();
        EventProducer.getInstance().setQueue(queue);
        EventConsumer.getInstance()
                .setQueue(queue)
                .register(new SendMessageHandler())
                .consume();

        System.out.println("Server is starting...");
        new ChatServer().start();
    }


    public void start() throws IOException {
        isRunning = true;
        // 定期清除任务
        autoClearService.scheduleWithFixedDelay(new AutoClearClientTask(), 20, 20, TimeUnit.SECONDS);

        ServerSocket server = new ServerSocket(8888);
        System.out.println("Server is running...");
        while (isRunning) {
            Socket clientSocket = server.accept();
            Client client = new Client(clientSocket);
            clients.add(client);
            // 交给线程池处理
            pool.submit(new ClientHandler(client));
            System.out.println("A client has entered");
        }
    }

    // 将消息发送给非发送者的Client
    private synchronized void broadcastMessage(Message message, Client except) {
        for (Client client : clients) {
//            // 发送者不需要被广播
//            if (client == null || client == except || !client.available()) {
//                continue;
//            }
            try {
                client.sendMessage(message);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 负责处理 一个客户端的请求
     */
    class ClientHandler implements Runnable {

        private Client client;

        public ClientHandler(Client client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                while (client.available()) {
                    // 持续读取 客户端信息
                    Message message = (Message) client.getInput().readObject();
                    System.out.println(message);

                    // 将消息广播
                    // broadcastMessage(message, client);

                    // 创建事件
                    EventModel event = new EventModel();
                    event.setEventType(EventType.SEND_MESSAGE)
                            .setMessage(message)
                            .setClients(new ArrayList<>(clients));
                    // 将事件交给消息队列处理
                    EventProducer.getInstance().fireEvent(event);

                }
            } catch (IOException e) {
            } catch (ClassNotFoundException e) {
            } finally {
                if (client != null) {
                    client.close();
                }
            }
        }
    }

    /**
     * 定期清理 不可用的 Client
     */
    class AutoClearClientTask implements Runnable {
        @Override
        public void run() {
            System.out.println("Begin to remove disconnected clients");
            List<Client> toRemove = new LinkedList<>();
            for (Client client : clients) {
                if (client == null || !client.available()) {
                    toRemove.add(client);
                }
            }
            clients.removeAll(toRemove);
        }
    }

    /**
     * 关闭 server
     */
    public void shutdown() {
        isRunning = false;
        for (Client client : clients) {
            if (client != null || !client.available()) {
                client.close();
            }
        }
        autoClearService.shutdown();
        pool.shutdown();
    }
}
