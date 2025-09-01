package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import server.ChatManager.ChatSession; // Import the inner class

public class SocketData {
    private Socket socket;
    private BufferedReader inputStream;
    private PrintStream outputStream;
    private String clientAddress;
    private boolean available = true;
    private String name;

    // --- MODIFIED FIELDS ---
    private ChatSession currentSession = null; // Reference to the current chat session
    private SocketData pendingRequestTo = null; // Tracks an outgoing chat request

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

    // --- NEW AND MODIFIED METHODS ---
    public ChatSession getCurrentSession() {
        return currentSession;
    }

    public void setCurrentSession(ChatSession currentSession) {
        this.currentSession = currentSession;
    }

    public SocketData getPendingRequestTo() {
        return pendingRequestTo;
    }

    public void setPendingRequestTo(SocketData pendingRequestTo) {
        this.pendingRequestTo = pendingRequestTo;
    }
}