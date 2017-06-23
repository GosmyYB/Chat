package com.wyb.demo.chat.server;

import com.wyb.demo.async.EventConsumer;
import com.wyb.demo.async.EventModel;
import com.wyb.demo.async.EventProducer;
import com.wyb.demo.async.EventType;
import com.wyb.demo.async.handler.BroadcastMessageHandler;
import com.wyb.demo.model.Client;
import com.wyb.demo.model.Message;
import com.wyb.demo.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 聊天室服务器
 * Created by wyb.
 */
public class ChatServer {

    private static final Logger logger = LoggerFactory.getLogger(ChatServer.class);

    /**
     * 客户列表
     */
    private List<Client> clients = new CopyOnWriteArrayList<>();

    /**
     * 线程池 为多个客户端提供线程服务
     */
    private ExecutorService pool = Executors.newCachedThreadPool();

    /**
     * 定时清除客户端连接线程
     */
    private ScheduledExecutorService autoClearService = Executors.newSingleThreadScheduledExecutor();
    /**
     * 清理客户的间隔时间
     */
    private static final int AUTO_CLEAN_INTERVAL = 3;

    /**
     * 可用标记位
     */
    private boolean isRunning;

    /**
     * 程序入口
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        BlockingQueue<EventModel> queue = new LinkedBlockingDeque<>();
        EventProducer.getInstance().setQueue(queue);
        EventConsumer.getInstance()
                .setQueue(queue)
                .register(new BroadcastMessageHandler())
                .consume();

        logger.info("Server is starting...");
        new ChatServer().start();
    }

    /**
     * 启动服务器
     * @throws IOException
     */
    public void start() throws IOException {
        isRunning = true;
        // 定期清除任务
        autoClearService.scheduleWithFixedDelay(new AutoClearClientTask(), 3, AUTO_CLEAN_INTERVAL, TimeUnit.SECONDS);

        ServerSocket server = new ServerSocket(8888);
        logger.info("Server is running...");
        while (isRunning) {
            Socket clientSocket = server.accept();
            Client client = new Client(clientSocket);
            clients.add(client);
            // 交给线程池处理.
            pool.submit(new ClientHandler(client));
            logger.info("Client: " + client.getSocket() + " has entered");
        }
    }

    /**
     * 同步地广播接收到的消息。
     * 已改用异步消息队列处理.
     * @param message
     * @param except
     */
    @Deprecated
    private synchronized void broadcastMessage(Message message, Client except) {
        for (Client client : clients) {
            try {
                client.sendMessage(message);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 客户端处理类
     * 每个对象为一个客户端服务
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
                    logger.info(message.toString());

                    String body = message.getBody();
                    // 接收到断开连接消息
                    if (Constant.DISCONNECT_MSG.equals(body)) {
                        clients.remove(client);
                        client.close();
                        break;
                    }
                    // 创建事件
                    EventModel event = new EventModel();
                    event.setEventType(EventType.BROADCAST_MESSAGE)
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
            List<Client> toRemove = new LinkedList<>();
            for (Client client : clients) {
                if (!client.available()) {
                    logger.info("Removing client" + client.getSocket().getInetAddress());
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
