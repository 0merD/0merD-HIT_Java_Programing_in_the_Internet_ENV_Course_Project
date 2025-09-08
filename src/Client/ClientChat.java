package client;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class ClientChat {

    private final String host;
    private final int port;
    private Socket socket;
    private BufferedReader fromServer;
    private PrintStream toServer;
    private Scanner consoleInputStream;
    private volatile boolean isRunning = true;

    public ClientChat(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {
        try {
            consoleInputStream = new Scanner(System.in);

            socket = new Socket(host, port);
            System.out.printf("Connected to server at %s:%d%n", socket.getInetAddress(), socket.getPort());

            toServer = new PrintStream(socket.getOutputStream(), true);
            fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Listener thread: read server lines and print; show popups for chat invites
            Thread serverListener = new Thread(() -> {
                try {
                    String serverMsg;
                    while ((serverMsg = readServerLine()) != null) {
                        System.out.println(serverMsg);

                        // Simple heuristics to catch chat invite prompts/notifications
                        final String msg = serverMsg.trim().toLowerCase();
                        if (msg.contains("wants to chat") || msg.contains("respond to chat invite")) {
                            final String serverMsgForDialog = serverMsg; // effectively final for lambda
                            SwingUtilities.invokeLater(() -> {
                                int ans = JOptionPane.showConfirmDialog(
                                        null,
                                        serverMsgForDialog,
                                        "Chat Invite",
                                        JOptionPane.YES_NO_OPTION
                                );
                                sendLine(ans == JOptionPane.YES_OPTION ? "yes" : "no");
                            });
                        }
                    }
                } finally {
                    isRunning = false;
                    closeQuietly();
                }
            }, "ServerListener");
            serverListener.setDaemon(true);
            serverListener.start();

            // Input thread: send anything user types to the server
            Thread inputSender = new Thread(() -> {
                try {
                    while (isRunning) {
                        if (!consoleInputStream.hasNextLine()) {
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException ignored) { }
                            continue;
                        }
                        String line = consoleInputStream.nextLine();
                        if ("exit".equalsIgnoreCase(line)) {
                            // Local exit: close the socket; server will detect disconnection
                            break;
                        }
                        sendLine(line);
                    }
                } finally {
                    isRunning = false;
                    closeQuietly();
                }
            }, "ConsoleInputSender");
            inputSender.setDaemon(true);
            inputSender.start();

            // Wait until socket closes
            while (isRunning && !socket.isClosed()) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ignored) { }
            }
            System.out.println("Disconnected from server.");
        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
        }
    }

    private synchronized void sendLine(String text) {
        if (toServer != null) {
            toServer.println(text);
        }
    }

    private String readServerLine() {
        try {
            return fromServer.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    private void closeQuietly() {
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) { }
    }

    public static void main(String[] args) {
        // Single unified port used by the server-side session handler
        new ClientChat("localhost", 1234).start();
    }
}