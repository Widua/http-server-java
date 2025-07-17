package connection;

import java.util.HashMap;
import java.util.Map;

public class ResponseBuilder {
    private Map<String, String> headers = new HashMap<>();
    private String httpStatus = "200 OK";
    private String httpBody = "";
    private final String HTTP_VERSION = "HTTP/1.1 ";
    private String httpBodyType;

    public void addHeader(String key, String value){
        headers.put(key, value);
    }

    public void setHttpStatus(String httpStatus) {
        this.httpStatus = httpStatus;
    }

    public void setBody(String httpBody,String httpBodyType,Integer contentLength) {
        this.httpBody = httpBody;
        this.httpBodyType = httpBodyType;

        headers.put("Content-Type",httpBodyType);
        headers.put("Content-Length",String.valueOf(contentLength));
    }

    @Override
    public String toString() {
        StringBuilder builder = getResponseHead();
        builder.append("\r\n");
        builder.append(httpBody);
        return builder.toString();
    }

    public StringBuilder getResponseHead(){
        StringBuilder builder = new StringBuilder();
        builder.append(HTTP_VERSION);
        builder.append(httpStatus);
        builder.append("\r\n");
        headers.forEach(
                (k, v) -> {
                    builder.append(k).append(": ").append(v).append("\r\n");
                }
        );

        return builder;
    }

}
