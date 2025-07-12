package connection;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class ClientHandler implements Runnable {
    private final String HTTP_VERSION = "HTTP/1.1 ";
    private PrintWriter output;
    private BufferedReader input;


    public ClientHandler(PrintWriter output, BufferedReader input){
        this.output = output;
        this.input = input;
    }

    @Override
    public void run() {
        System.out.println("accepted new connection");
        output.println(HTTP_VERSION+"200 OK\r\n\r\n");
    }
}
