import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        System.out.println("Logs from your program will appear here!");
        final String HTTP_VERSION = "HTTP/1.1 ";
        final String STATUS = "200 OK";
        try {
            ServerSocket serverSocket = new ServerSocket(4221);
            serverSocket.setReuseAddress(true);

            Socket clientSocket = serverSocket.accept();

            try(
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(),true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    ){
                 out.println(HTTP_VERSION+STATUS+"\r\n\r\n");
            }
            System.out.println("accepted new connection");
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
