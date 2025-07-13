package connection;

import java.util.Map;

public class ResponseBuilder {
    private Map<String,String> headers;
    private String httpStatus;
    private String httpBody;
    private final String HTTP_VERSION = "HTTP/1.1 ";

    public ResponseBuilder(Map<String, String> headers, String httpStatus, String httpBody) {
        this.headers = headers;
        this.httpStatus = httpStatus;
        this.httpBody = httpBody;
        if (!httpBody.isEmpty()){
            prepareBodyHeaders();
        }
    }

    private void prepareBodyHeaders(){
        headers.put("Content-Type","text/plain");
        headers.put("Content-Length", String.valueOf(httpBody.length()));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(HTTP_VERSION);
        builder.append(httpStatus);
        builder.append("\r\n");
        headers.forEach(
                (k,v) -> {
                    builder.append(k).append(": ").append(v).append("\r\n");
                }
        );
        builder.append("\r\n");
        builder.append(httpBody);

        return builder.toString();
    }
}
