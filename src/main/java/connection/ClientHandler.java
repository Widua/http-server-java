package connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
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
                case "/user-agent" ->{
                    httpBody = request.get("User-Agent");
                }
                default -> {
                    httpStatus = "404 Not Found";
                }
            }

            ResponseBuilder response = new ResponseBuilder(request, httpStatus, httpBody);
            output.println(response);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private Map<String, String> parseRequest() throws IOException {
        Map<String, String> request = new HashMap<>();

        StringBuilder req = new StringBuilder();
        int s = 0;
        while (input.ready()){
            s = input.read();
            req.append((char)s);
        }
        String[] splitted = req.toString().split("\r\n");

        if (splitted.length == 1){
            request.put("Endpoint","/");
            return request;
        }
        String[] head = splitted[0].split(" ");

        request.put("Method",head[0]);
        request.put("Endpoint",head[1]);
        request.put("HTTP_VERSION",head[2]);
        int eohIndex = 1;
        for (String header: Arrays.copyOfRange(splitted, 1, splitted.length)) {
            eohIndex ++;
            if (header.isEmpty()){
                break;
            }
            String[] headerCon = header.split(": ");
            request.put(headerCon[0],headerCon[1]);
        }

        if (eohIndex < splitted.length){
            request.put("Body",splitted[eohIndex]);
        }
        return request;
    }

}
