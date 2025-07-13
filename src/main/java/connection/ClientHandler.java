package connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ClientHandler implements Runnable {
    private PrintWriter output;
    private BufferedReader input;

    public ClientHandler(PrintWriter output, BufferedReader input) {
        this.output = output;
        this.input = input;
    }

    @Override
    public void run() {
        String httpStatus = "200 OK";
        String httpBody = "";
        System.out.println("accepted new connection");
        try {
            Map<String, String> request = parseRequest();
            String endpoint = request.get("Endpoint");
            switch (endpoint) {
                case String s when Pattern
                        .matches("/echo/[A-z0-9]*", s) -> {
                    httpBody = s.replaceAll("/echo/", "");

                }
                case "/" -> {

                }
                default -> {
                    httpStatus = "404 Not Found";
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Map<String, String> headers = new HashMap<>();

        ResponseBuilder response = new ResponseBuilder(headers, httpStatus, httpBody);

        output.println(response);
    }

    private Map<String, String> parseRequest() throws IOException {
        Map<String, String> request = new HashMap<>();

        String[] header = input.readLine().split(" ");

        request.put("Method", header[0]);
        request.put("Endpoint", header[1]);
        request.put("HTTP_VERSION", header[2]);

        return request;
    }

}
