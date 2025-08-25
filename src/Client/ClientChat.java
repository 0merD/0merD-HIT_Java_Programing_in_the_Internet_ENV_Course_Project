package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Date;

public class ClientChat {
    public static void main(String[] args) {
        Socket socket = null;
        BufferedReader fromNetInputStream;   // for reading data from server
        BufferedReader consoleInput;         // for receiving data from user through console
        PrintStream toNetOutputStream;       // for sending data to server

        try {
            socket = new Socket("localhost", 7000); // connect to server
            System.out.println(new Date() + " ---> Connected to server at " +
                    socket.getInetAddress() + ":" + socket.getPort());

            fromNetInputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            toNetOutputStream = new PrintStream(socket.getOutputStream());
            consoleInput = new BufferedReader(new InputStreamReader(System.in));

            // Start thread for listening to server messages
            Thread serverListener = new Thread(() -> {
                try {
                    String serverMsg;
                    while ((serverMsg = fromNetInputStream.readLine()) != null) {
                        System.out.println("\n[SERVER] " + serverMsg);
                        System.out.print("> ");
                    }
                } catch (IOException e) {
                    System.out.println("Connection to server lost.");
                }
            });
            serverListener.start();

            // Main thread: handle user input
            String line = "";

            // --- MODIFICATION HERE ---
            // The loop should continue indefinitely.
            while (true) {
                System.out.print("> ");
                line = consoleInput.readLine();
                if (line == null) break;

                // If the user wants to truly exit, they can type "exit".
                // This is a new command only for the client-side.
                if (line.equalsIgnoreCase("exit")) {
                    break;
                }

                toNetOutputStream.println(line);
            }
            // --- END OF MODIFICATION ---

            socket.close();
            System.out.println("Client closed connection.");

        } catch (Exception e) {
            System.err.println("Error: " + e);
        }
    }
}