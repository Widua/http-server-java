package connection;

import configuration.ServerSettings;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ClientHandler implements Runnable {
    private PrintWriter output;
    private BufferedReader input;
    private ServerSettings settings = ServerSettings.getInstance();
    Map<String,String> request;
    ResponseBuilder response = new ResponseBuilder();

    public ClientHandler(PrintWriter output, BufferedReader input) {
        this.output = output;
        this.input = input;
        try {
            this.request = parseRequest();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        System.out.println("accepted new connection");
        try {
            String endpoint = request.get("Endpoint");
            switch (endpoint) {
                case String s when Pattern
                        .matches("/echo/[A-z0-9]*", s) -> {
                    response.setBody(s.replaceAll("/echo/", ""), "text/plain");
                }
                case "/" -> {

                }
                case String s when Pattern.matches("/files/\\S*", s) -> {
                    String method = request.get("Method");

                    switch (method){
                        case "GET" -> fileReader(s.replaceAll("/files/", ""));
                        case "POST" -> fileWriter(s.replaceAll("/files/",""));
                    }

                    return;
                }
                case "/user-agent" -> {
                    response.setBody(request.get("User-Agent"), "text/plain");
                }
                default -> {
                    response.setHttpStatus("404 Not Found");
                }
            }

            output.println(response);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private Map<String, String> parseRequest() throws IOException {
        Map<String, String> request = new HashMap<>();

        StringBuilder req = new StringBuilder();
        int s = 0;
        while (input.ready()) {
            s = input.read();
            req.append((char) s);
        }
        String[] splitted = req.toString().split("\r\n");

        if (splitted.length == 1) {
            request.put("Endpoint", "/");
            return request;
        }
        String[] head = splitted[0].split(" ");

        request.put("Method", head[0]);
        request.put("Endpoint", head[1]);
        request.put("HTTP_VERSION", head[2]);
        int eohIndex = 1;
        for (String header : Arrays.copyOfRange(splitted, 1, splitted.length)) {
            eohIndex++;
            if (header.isEmpty()) {
                break;
            }
            String[] headerCon = header.split(": ");
            request.put(headerCon[0], headerCon[1]);
        }

        if (eohIndex < splitted.length) {
            request.put("Body", splitted[eohIndex]);
        }
        return request;
    }

    private void fileWriter(String fileName) throws IOException {
        File file = new File(Path.of(settings.getSetting("directory"), fileName).toUri());
        String body = request.get("Body");
        int contentLength = Integer.parseInt(request.get("Content-Length"));
        Files.writeString(file.toPath(), body.substring(0,contentLength));

        response.setHttpStatus("201 Created");
        output.println(response);
    }

    private void fileReader(String fileName) throws IOException {
        File file = new File(Path.of(settings.getSetting("directory"), fileName).toUri());

        if (file.exists()) {
            byte[] fileContent = Files.readAllBytes(file.toPath());
            StringBuilder builder = response.getResponseHead();
            builder.append("Content-Type: application/octet-stream").append("\r\n");
            builder.append("Content-Length: ").append(fileContent.length).append("\r\n");
            builder.append("\r\n");
            builder.append(new String(fileContent));
            output.println(builder);
        } else {
            response.setHttpStatus("404 Not Found");
            output.println(response);
        }
    }
}
