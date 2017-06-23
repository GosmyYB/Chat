package com.wyb.demo.chat.client;

import com.wyb.demo.chat.server.ChatServer;
import com.wyb.demo.model.Message;
import com.wyb.demo.util.Constant;
import com.wyb.demo.util.IOUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * 聊天室客户端
 * Created by wyb.
 */
public class ChatClient extends Application {

    private static final Logger logger = LoggerFactory.getLogger(ChatClient.class);

    // Host name or ip 192.168.1.100
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;

    private static final String NEW_LINE_CHARACTER;

    static {
        if (System.getProperty("os.name").equals("Mac OS X")) {
            // Linux 使用 \n 作为换行符
            NEW_LINE_CHARACTER = "\n";
        } else {
            // Windows 使用 \r\n 作为换行符
            NEW_LINE_CHARACTER = "\r\n";
        }
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    /**
     * 用户名输入框
     */
    private TextField tfName = new TextField();
    /**
     * 消息内容输入框
     */
    private TextField tfText = new TextField();
    /**
     * 消息展示窗口
     */
    private TextArea messageArea = new TextArea();

    private Socket socket;
    private ObjectOutputStream toServer;
    private ObjectInputStream fromServer;

    /**
     * 可用标记位
     */
    private boolean isRunning = true;

    /**
     * 启动客户端，创建 GUI
     * @param primaryStage
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {

        // 创建 GUI
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(5);
        gridPane.setVgap(5);
        gridPane.add(new Label("Name"), 2, 0);
        gridPane.add(tfName, 3, 0);
        gridPane.add(new Label("Enter text"), 2, 1);
        gridPane.add(tfText, 3, 1);

        // 左对齐
        gridPane.setAlignment(Pos.BASELINE_LEFT);
        // 设置文本框大小
        tfName.setPrefColumnCount(3);
        tfText.setPrefColumnCount(15);

        BorderPane mainPane = new BorderPane();
        mainPane.setTop(gridPane);
        // messageArea is read only
        messageArea.setEditable(false);
        mainPane.setCenter(new ScrollPane(messageArea));

        Scene scene = new Scene(mainPane, 450, 200);
        primaryStage.setTitle("Chat");
        primaryStage.setScene(scene);
        primaryStage.show();

        connectToServer();

        // 对 每个 ActionEvent e 执行 sendMessage()方法
        tfText.setOnAction(e -> sendMessage());

        // 开一个线程负责接收消息, 以免阻塞主线程
        new Thread(new ReceiveMessage()).start();
    }

    /**
     * 客户端退出时被执行
     * @see Application#stop()
     * @throws Exception
     */
    @Override
    public void stop() throws Exception {
        disconnect();
    }

    /**
     * 发送文本框消息
     */
    private void sendMessage() {
        String from = tfName.getText().trim();
        String body = tfText.getText().trim();
        // 发送给服务器端
        sendMessage(from, body);
        // 清空文本
        tfText.clear();
    }

    /**
     * 发送消息给服务器
     *
     * @param from
     * @param body
     */
    private void sendMessage(String from, String body) {
        // 发送给服务器端
        try {
            toServer.writeObject(new Message(from, body));
            toServer.flush();
        } catch (IOException e) {
        }
    }

    // 负责处理接收消息
    class ReceiveMessage implements Runnable {
        @Override
        public void run() {
            try {
                while (isRunning) {
                    // fromServer 会被阻塞
                    Message message = (Message) fromServer.readObject();
                    messageArea.appendText(message.getFrom() + ": " + message.getBody() + NEW_LINE_CHARACTER);
                    //logger.info(message);
                }
            } catch (IOException ex) {
            } catch (ClassNotFoundException ex) {
            } finally {
                disconnect();
            }
        }
    }

    /**
     * 连接到服务器端
     */
    private void connectToServer() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            toServer = new ObjectOutputStream(socket.getOutputStream());
            fromServer = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            disconnect();
        }
    }

    /**
     * 断开与服务器端的连接
     */
    private void disconnect() {
        isRunning = false;
        sendMessage("", Constant.DISCONNECT_MSG);
        messageArea.appendText("###### You have disconnected from server ######");
        IOUtil.closeAll(toServer, fromServer, socket);
    }

    /**
     * 用于进行消息发送压力测试
     */
    public void testSendMessage() {
        // 连接到服务器
        connectToServer();
        new Thread(new ReceiveMessage()).start();
        for (int i = 0; i < 1000; i++) {
            sendMessage(Thread.currentThread().getName(), "Hello" + i);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
