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

    // Client identity for handshake
    private String username;
    private String password;

    public ClientChat(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {
        try {
            consoleInputStream = new Scanner(System.in);

            // Collect credentials; server resolves user type and branch
            System.out.print("Enter username: ");
            username = consoleInputStream.nextLine().trim();
            System.out.print("Enter password: ");
            password = consoleInputStream.nextLine().trim();

            socket = new Socket(host, port);
            System.out.printf("Connected to server at %s:%d%n", socket.getInetAddress(), socket.getPort());

            toServer = new PrintStream(socket.getOutputStream());
            fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send handshake (username + password). Server authenticates.
            toServer.println("HELLO " + username + " " + password);

            // Listener thread 1: server messages + pop-up dialog on chat requests
            Thread serverListener = new Thread(() -> {
                try {
                    String serverMsg;
                    while ((serverMsg = fromServer.readLine()) != null) {
                        // Popup on chat request
                        if (serverMsg.contains("wants to chat with you. Reply 'yes' or 'no'.")) {
                            System.out.println("[SERVER] " + serverMsg);
                            final String msgForDialog = serverMsg;
                            SwingUtilities.invokeLater(() -> {
                                int ans = JOptionPane.showConfirmDialog(
                                        null,
                                        msgForDialog,
                                        "Chat Request",
                                        JOptionPane.YES_NO_OPTION
                                );
                                if (ans == JOptionPane.YES_OPTION) {
                                    toServer.println("yes");
                                } else {
                                    toServer.println("no");
                                }
                            });
                        } else {
                            System.out.println(serverMsg);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Connection to server lost.");
                } finally {
                    isRunning = false;
                }
            });
            serverListener.setDaemon(true);
            serverListener.start();

            // Listener thread 2: health watcher
            Thread socketWatcher = new Thread(() -> {
                try {
                    while (isRunning && !socket.isClosed()) {
                        Thread.sleep(500);
                    }
                } catch (InterruptedException ignored) { }
            });
            socketWatcher.setDaemon(true);
            socketWatcher.start();

            // Main thread: number-based menu, with passthrough for free text (yes/no/goodbye/messages)
            while (isRunning) {
                printMenu();
                String choice = consoleInputStream.nextLine().trim();

                if (choice.equalsIgnoreCase("exit")) {
                    // Local-only exit
                    break;
                }

                switch (choice) {
                    case "1": // list available
                        toServer.println("list");
                        break;
                    case "2": // listall with who is chatting with whom
                        toServer.println("listall");
                        break;
                    case "3": // request chat
                        System.out.print("Enter target client name (username@branch): ");
                        String targetName = consoleInputStream.nextLine().trim();
                        toServer.println("chat " + targetName);
                        break;
                    case "4": // join (ShiftManager only; server enforces)
                        System.out.print("Enter a participant client name (username@branch) of the active chat to join: ");
                        String joinTarget = consoleInputStream.nextLine().trim();
                        toServer.println("join " + joinTarget);
                        break;
                    case "5": // savechat
                        toServer.println("savechat");
                        break;
                    case "6": // goodbye (text-based command per request)
                        System.out.println("Type 'goodbye' to leave/end the chat:");
                        String gb = consoleInputStream.nextLine().trim();
                        toServer.println(gb); // forward as-is so server handles it
                        break;
                    default:
                        // Passthrough any non-numeric input (e.g., "yes", "no", or message text)
                        toServer.println(choice);
                        break;
                }
            }

            isRunning = false;
            try {
                socket.close();
            } catch (IOException ignored) {}
            System.out.println("Disconnected from server.");

        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("Menu:");
        System.out.println("  1) List available clients");
        System.out.println("  2) List all clients (and who is chatting with whom)");
        System.out.println("  3) Request chat with a client");
        System.out.println("  4) Join an active chat (ShiftManager only)");
        System.out.println("  5) Save current chat upon completion");
        System.out.println("  6) Say 'goodbye' (end/leave chat)");
        System.out.print("Choose an option (or type 'exit' to close client): ");
    }

    public static void main(String[] args) {
        new ClientChat("localhost", 7000).start();
    }
}