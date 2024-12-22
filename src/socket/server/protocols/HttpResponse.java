package socket.server.protocols;

import java.io.*;

public class HttpResponse {
    private final OutputStream output;

    public HttpResponse(OutputStream output) {
        this.output = output;
    }

    public void sendResponse(int statusCode, String statusMessage, String contentType, byte[] content) throws IOException {
        PrintWriter writer = new PrintWriter(output, true);
        writer.println("HTTP/1.1 " + statusCode + " " + statusMessage);
        writer.println("Content-Type: " + contentType);
        writer.println("Content-Length: " + content.length);
        writer.println();
        writer.flush();
        output.write(content);
        output.flush();
    }

    public void sendError(int statusCode, String message) throws IOException {
        String content = "<h1>" + statusCode + " " + message + "</h1>";
        sendResponse(statusCode, message, "text/html", content.getBytes());
    }
}
