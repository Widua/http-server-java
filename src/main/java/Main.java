import configuration.ServerSettings;
import connection.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        System.out.println("Logs from your program will appear here!");

        ServerSettings settings = ServerSettings.getInstance();

        for (int j = 0; j < args.length; j++) {
            if (args[j].equals("--directory")) {
                settings.addSetting("directory", args[j + 1]);
            }
        }

        try {
            ServerSocket serverSocket = new ServerSocket(4221);
            serverSocket.setReuseAddress(true);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                clientSocket.setKeepAlive(true);
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
