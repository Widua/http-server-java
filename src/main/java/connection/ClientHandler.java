package connection;

import configuration.ServerSettings;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ClientHandler implements Runnable {
    private final ServerSettings settings = ServerSettings.getInstance();
    private Map<String, String> request;
    private ResponseBuilder response;
    private final Socket client;

    public ClientHandler(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        System.out.println("accepted new connection: "+Thread.currentThread().getName());
        try(
                BufferedWriter output = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()) );
                BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));
                ) {
            while (!client.isClosed()) {
                this.request = parseRequest(input);
                if (!request.containsKey("Endpoint")) {
                    break;
                }
                response = new ResponseBuilder();
                String endpoint = request.get("Endpoint");
                switch (endpoint) {
                    case String s when Pattern
                            .matches("/echo/[A-z0-9]*", s) -> {
                        String body = s.replaceAll("/echo/", "");
                        response.setBody(body, "text/plain", body.length());
                    }
                    case "/" -> {

                    }
                    case String s when Pattern.matches("/files/\\S*", s) -> {
                        String method = request.get("Method");
                        switch (method) {
                            case "GET" -> fileReader(s.replaceAll("/files/", ""));
                            case "POST" -> fileWriter(s.replaceAll("/files/", ""));
                        }
                    }
                    case "/user-agent" -> {
                        String userAgent = request.get("User-Agent");
                        response.setBody(userAgent, "text/plain", userAgent.length());
                    }
                    default -> {
                        response.setHttpStatus("404 Not Found");
                    }
                }
                output.write(response.toString());
                output.flush();
                if (request.getOrDefault("Connection", "").equalsIgnoreCase("close")) {
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private Map<String, String> parseRequest(BufferedReader input) throws IOException {
        Map<String, String> request = new HashMap<>();

        String head = input.readLine();

        if (head == null){
            return request;
        }

        String[] headPart = head.split(" ");

        request.put("Method",headPart[0]);
        request.put("Endpoint",headPart[1]);
        request.put("HTTP_VERSION",headPart[2]);

        String headerLine;
        while ( (headerLine = input.readLine()) != null && !headerLine.isEmpty() ){
            String[] header = headerLine.split(": ");
            request.put(header[0],header[1]);
        }

        if (request.containsKey("Content-Length")){
            int contentLength = Integer.parseInt(request.get("Content-Length"));
            char[] requestBody = new char[contentLength];
            int read = input.read(requestBody,0,contentLength);
            request.put("Body",new String(requestBody,0,read));
        }
        return request;
    }

    private void fileWriter(String fileName) throws IOException {
        File file = new File(Path.of(settings.getSetting("directory"), fileName).toUri());
        String body = request.get("Body");
        int contentLength = Integer.parseInt(request.get("Content-Length"));
        Files.writeString(file.toPath(), body.substring(0, contentLength));

        response.setHttpStatus("201 Created");
    }

    private void fileReader(String fileName) throws IOException {
        File file = new File(Path.of(settings.getSetting("directory"), fileName).toUri());

        if (!file.exists()) {
            response.setHttpStatus("404 Not Found");
            return;
        }
        byte[] fileContent = Files.readAllBytes(file.toPath());
        response.setHttpStatus("200 OK");
        response.setBody(new String(fileContent), "application/octet-stream", fileContent.length);
    }
}
