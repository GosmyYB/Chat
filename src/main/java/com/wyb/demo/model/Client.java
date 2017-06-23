package com.wyb.demo.model;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by wyb.
 */
public class Client implements Closeable {

    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;

    private volatile boolean isRunning;

    public Client(Socket socket) {
        try {
            this.socket = socket;
            this.input = new ObjectInputStream(socket.getInputStream());
            this.output = new ObjectOutputStream(socket.getOutputStream());
            this.isRunning = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Client(Socket socket, ObjectInputStream input, ObjectOutputStream output) {
        this.socket = socket;
        this.input = input;
        this.output = output;
        this.isRunning = true;
    }

    public Socket getSocket() {
        return socket;
    }

    public ObjectInputStream getInput() {
        return input;
    }

    public ObjectOutputStream getOutput() {
        return output;
    }

    public void sendMessage(Message message) throws IOException {
        output.writeObject(message);
        output.flush();
    }

    @Override
    public void close() {
        isRunning = false;
        try {
            if (output != null) {
                output.close();
                output = null;
            }
            if (input != null) {
                input.close();
                input = null;
            }
            if (socket != null) {
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            isRunning = false;
        }
    }

    public boolean available() {
        return isRunning;
    }
}
