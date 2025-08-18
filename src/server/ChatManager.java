package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Vector;

public class ChatManager {
    static Vector<SocketData> allConnections = new Vector<SocketData>();

    public static void main(String[] args) throws IOException {
        final ServerSocket server = new ServerSocket(7000);
        System.out.println(new Date() + "Server waiting for connections...");
        while (true) {
            final Socket socket = server.accept();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    SocketData currentSocketData = new SocketData(socket);
                    allConnections.add(currentSocketData);
                    System.out.println("Client " + currentSocketData.getClientAddress() + " :'IM IN'" + new Date());

                    currentSocketData.getOutputStream().println("Welcome to the server Mr.Client ...");

                    String line = "";

                    try {
                        while (!line.equals("goodbye")) {
                            line = currentSocketData.getInputStream().readLine();
                            sendBroadcastMessage(line, currentSocketData);
                            System.out.println(new Date() + "From client: " + currentSocketData.getClientAddress() + ": " + line);


                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }
    }

    public static void sendBroadcastMessage(String theMessage, SocketData theSender){
        for (SocketData sd: allConnections)
            if (sd.getSocket().isConnected()){
                sd.getOutputStream().println( theSender.getClientAddress() +"@"+ theMessage);
            }
    }
}


