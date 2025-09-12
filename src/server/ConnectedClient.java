package server;

import server.enums.UserType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class ConnectedClient {
    private Socket socket;
    private BufferedReader inputStream;
    private PrintStream outputStream;
    private String clientAddress;
    private boolean available = true;
    private String name;

    public boolean isInChatMode() {
        return inChatMode;
    }

    public void setInChatMode(boolean inChatMode) {
        this.inChatMode = inChatMode;
    }

    private boolean inChatMode; // should be volatile?

    // ... existing code ...
    private ChatSession currentSession = null; // Reference to the current chat session
    private ConnectedClient pendingRequestTo = null; // Tracks an outgoing chat request
    // ... existing code ...

    // Add user metadata so CommandHandler can read the user type
    private UserType userType = null;
    private Integer branchNumber = null;

    public ConnectedClient(Socket socket) {
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

    public ChatSession getCurrentSession() {
        return currentSession;
    }

    public void setCurrentSession(ChatSession currentSession) {
        this.currentSession = currentSession;
    }

    public ConnectedClient getPendingRequestTo() {
        return pendingRequestTo;
    }

    public void setPendingRequestTo(ConnectedClient pendingRequestTo) {
        this.pendingRequestTo = pendingRequestTo;
    }

    // Add these getters/setters
    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public Integer getBranchNumber() {
        return branchNumber;
    }

    public void setBranchNumber(Integer branchNumber) {
        this.branchNumber = branchNumber;
    }
}