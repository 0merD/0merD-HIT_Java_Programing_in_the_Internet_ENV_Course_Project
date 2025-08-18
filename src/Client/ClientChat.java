package Client;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;

public class ClientChat {
    public static void main(String[] args){
    Socket socket = null;
    DataInputStream fromNetInputStream; //for reading data from server
    DataInputStream consoleInput; //for receiving data from user through console
    PrintStream toNetOutputStream; //for sending data to server
    String line = "";

    try {
        socket = new Socket("localhost",7000); //connect to the server at IP, port (localhost means 127.0.0.1, loopback)
        System.out.println(new Date() + " ---> Connected to server at " + socket.getLocalAddress() + ":" + socket.getLocalPort());
        fromNetInputStream = new DataInputStream(socket.getInputStream()); //input/output objects derived from socket data
        toNetOutputStream = new PrintStream(socket.getOutputStream()); //input/output objects derived from socket data
        consoleInput = new DataInputStream(System.in);

        System.out.println(new Date() + " ---> Received from server: "
            + fromNetInputStream.readLine()); //receive message from server
    while (!line.equals("goodbye")){ //get input from client until "goodbye" is entered
        System.out.println("Enter line: ");
        line = consoleInput.readLine(); //get line from console.input
        toNetOutputStream.println(line); // send text to server
        System.out.println(new Date() + " ---> Received from server: "
            + fromNetInputStream.readLine()); //receive text from server
        }
    } catch (Exception e) { System.err.println(e);//
    } finally { //always runs, even with errors
        try {
            socket.close();
            System.out.println("Client closed connection by inserting goodbye");
        } catch (IOException e) {}
        }
    }

}
