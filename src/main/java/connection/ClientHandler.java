package connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ClientHandler implements Runnable {
    private final String HTTP_VERSION = "HTTP/1.1";
    private PrintWriter output;
    private BufferedReader input;

    public ClientHandler(PrintWriter output, BufferedReader input) {
        this.output = output;
        this.input = input;
    }

    @Override
    public void run(){
        String httpStatus = "200 OK";
        System.out.println("accepted new connection");
        try {
            Map<String,String> request = parseRequest();
            if (!request.get("Endpoint").equals("/")){
                httpStatus = "404 Not Found";
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        output.println( String.format("%s %s\r\n\r\n",HTTP_VERSION,httpStatus ) );
    }

    private Map<String, String> parseRequest() throws IOException {
        Map<String, String> request = new HashMap<>();

        String[] header = input.readLine().split(" ");

        request.put("Method",header[0]);
        request.put("Endpoint",header[1]);
        request.put("HTTP_VERSION",header[2]);

        return request;
    }

}
