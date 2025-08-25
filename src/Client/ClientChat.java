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
                        // For a cleaner UI, we can use \r to clear the current line
                        // before printing the server message, then redraw the prompt.
                        // However, for simplicity, we'll stick to the newline.
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
            while (!line.equalsIgnoreCase("goodbye")) {
                // MODIFICATION: Use a simpler, less intrusive prompt.
                System.out.print("> ");
                line = consoleInput.readLine();
                if (line == null) break;
                toNetOutputStream.println(line);
            }

            socket.close();
            System.out.println("Client closed connection.");

        } catch (Exception e) {
            System.err.println("Error: " + e);
        }
    }
}