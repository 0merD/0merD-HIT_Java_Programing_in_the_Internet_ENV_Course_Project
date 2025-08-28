package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Date;
import java.util.Scanner;

public class ClientChat {

    private final String host;
    private final int port;
    private Socket socket;
    private BufferedReader fromServer;
    private PrintStream toServer;
    private Scanner consoleInputStream;

    public ClientChat(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {
        try {
            socket = new Socket(host, port);
            System.out.printf("Connected to server at %s:%d%n", socket.getInetAddress(), socket.getPort());

            toServer = new PrintStream(socket.getOutputStream());
            fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            consoleInputStream = new Scanner(System.in);

            Thread serverListener = new Thread(() -> {
                try {
                    String serverMsg;
                    while ((serverMsg = fromServer.readLine()) != null) {
                        System.out.println(serverMsg);
                    }
                } catch (IOException e) {
                    System.out.println("Connection to server lost.");
                }
            });
            serverListener.start();

            String line;
            while (true) {
                line = consoleInputStream.nextLine();
                if (line.equalsIgnoreCase("exit")) break;
                toServer.println(line);
            }

            socket.close();
            System.out.println("Disconnected from server.");

        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new ClientChat("localhost",1234).start();
    }
}

