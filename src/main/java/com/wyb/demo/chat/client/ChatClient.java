package com.wyb.demo.chat.client;

import com.wyb.demo.model.Message;
import com.wyb.demo.util.IOUtil;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by wyb.
 */
public class ChatClient extends Application {

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

    private TextField tfName = new TextField();
    private TextField tfText = new TextField();
    private TextArea messageArea = new TextArea();

    private Socket socket;
    private ObjectOutputStream toServer;
    private ObjectInputStream fromServer;

    // 控制 while 循环
    private boolean isRunning = true;

    @Override
    public void start(Stage primaryStage) throws Exception {

        // 创建 GUI
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(5);
        gridPane.setVgap(5);
        gridPane.add(new Label("Name"), 0, 0);
        gridPane.add(tfName, 2, 0);
        gridPane.add(new Label("Enter text"), 0, 1);
        gridPane.add(tfText, 2, 1);

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

        // 连接到服务器
        connectToServer();

        // 对 每个 ActionEvent e 执行 sendMessage()方法
        tfText.setOnAction(e -> sendMessage());

        // 开一个线程负责接收消息, 以免阻塞主线程
        new Thread(new ReceiveMessage()).start();
    }

    /**
     * 客户端退出时被执行
     * @throws Exception
     */
    @Override
    public void stop() throws Exception {
        shutdown();
        disconnect();
    }

    /**
     * 发送消息
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
     * @param from
     * @param body
     */
    private void sendMessage(String from, String body) {
        // 发送给服务器端
        try {
            toServer.writeObject(new Message(from, body));
            toServer.flush();
        } catch (IOException e) {
            disconnect();
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

    private void shutdown() {
        isRunning = false;
    }

    /**
     * 断开与服务器端的连接
     */
    private void disconnect() {
        sendMessage();
        IOUtil.closeAll(toServer, fromServer, socket);
    }
}
