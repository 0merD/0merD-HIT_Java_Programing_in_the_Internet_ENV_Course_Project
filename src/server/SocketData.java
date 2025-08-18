package server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

public class SocketData {
    private Socket socket;
    private DataInputStream inputStream;
    private PrintStream outputStream;
    private String ClientAddress;

    public SocketData(Socket socket){
        this.socket = socket;
        try {
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new PrintStream(socket.getOutputStream());
        }
        catch (IOException e){
            e.printStackTrace();
        }

        ClientAddress = socket.getInetAddress() + ":" + socket.getPort();

        }

    public Socket getSocket(){
    return socket;
    }

    public DataInputStream getInputStream(){
    return inputStream;
    }

    public PrintStream getOutputStream() {
    return outputStream;
    }

    public String getClientAddress(){
    return ClientAddress;
    }
}
