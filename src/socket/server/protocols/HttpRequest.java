package socket.server.protocols;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private String method;
    private String uri;
    private Map<String, String> headers;
    private String body;
    private boolean isFaviconRequest;

    private HttpRequest(String method, String uri, Map<String, String> headers, String body) {
        this.method = method != null ? method : "UNKNOWN";
        this.uri = uri != null ? uri : "/";
        this.headers = headers != null ? Map.copyOf(headers) : Collections.emptyMap();
        this.body = body != null ? body : "";

        // Check if this is a favicon request
    }

    public static HttpRequest parse(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));

        String requestLineString = reader.readLine();

        if (requestLineString == null || requestLineString.trim().isEmpty()) {
            return new HttpRequest("UNKNOWN", "/", Collections.emptyMap(), "");
        }

        requestLineString = requestLineString.trim();

        String[] requestLine = requestLineString.split(" ");
        if (requestLine.length < 3) {
            throw new IOException("Invalid request line: " + requestLineString);
        }

        String method = getMethod(requestLine[0]);
        String uri = requestLine[1];

        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            String[] header = line.split(": ", 2);
            if (header.length == 2) {
                headers.put(header[0].trim(), header[1].trim());
            }
        }

        StringBuilder bodyBuilder = new StringBuilder();
        if ("POST".equalsIgnoreCase(method)) {
            String contentLengthHeader = headers.get("Content-Length");
            if (contentLengthHeader != null) {
                try {
                    int contentLength = Integer.parseInt(contentLengthHeader);
                    char[] buffer = new char[contentLength];
                    int charsRead = reader.read(buffer, 0, contentLength);
                    if (charsRead > 0) {
                        bodyBuilder.append(buffer, 0, charsRead);
                    }
                } catch (NumberFormatException | IOException e) {
                    // Handle body reading errors
                }
            }
        }
        HttpRequest val = new HttpRequest(method, uri, headers, bodyBuilder.toString());
        val.isFaviconRequest = uri.equals("/favicon.ico");
        return val;
    }

    public static String getMethod(String method) {
        if (method.equalsIgnoreCase("et") || method.equalsIgnoreCase("get")) {
            return "GET";
        } else if (method.equalsIgnoreCase("ost") || method.equalsIgnoreCase("post")) {
            return "POST";
        }
        return "UNKNOWN";
    }

    public String getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public boolean isFaviconRequest() {
        return isFaviconRequest;
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public boolean hasHeader(String key) {
        return headers.containsKey(key);
    }

    @Override
    public String toString() {
        return "HttpRequest{" +
               "method='" + method + '\'' +
               ", uri='" + uri + '\'' +
               ", headers=" + headers +
               ", body='" + body + '\'' +
               ", isFaviconRequest=" + isFaviconRequest +
               '}';
    }
    public void setUri(String uri) {
        this.uri = uri;
    }
    public boolean isUnValidRequest () {
        return this.getMethod().equals("UNKNOWN");
    }
}