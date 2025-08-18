package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class SocketData {
    private Socket socket;
    private BufferedReader inputStream;
    private PrintStream outputStream;
    private String clientAddress;
    private boolean available = true;
    private String name;
    private SocketData chatPartner = null; // New field to hold the chat partner

    public SocketData(Socket socket) {
        this.socket = socket;
        try {
            inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outputStream = new PrintStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        clientAddress = socket.getInetAddress() + ":" + socket.getPort();
        name = clientAddress;
    }

    public Socket getSocket() {
        return socket;
    }

    public BufferedReader getInputStream() {
        return inputStream;
    }

    public PrintStream getOutputStream() {
        return outputStream;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getName() {
        return name;
    }

    public void setName(String n) {
        this.name = n;
    }

    public SocketData getChatPartner() {
        return chatPartner;
    }

    public void setChatPartner(SocketData chatPartner) {
        this.chatPartner = chatPartner;
    }
}