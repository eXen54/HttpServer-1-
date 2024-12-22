package socket.server.http.handler;

import socket.server.HttpServerManager;
import socket.server.Server;
import socket.server.protocols.HttpRequest;
import socket.server.protocols.HttpResponse;

import java.io.*;
import java.nio.file.Files;

public class StaticFileHandler implements ContentHandler {
    private final String contentType;
    public StaticFileHandler(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public void handle(Server server, String webRoot, HttpRequest request, HttpResponse response, HttpServerManager manager) throws IOException {
        if (request.getUri().isBlank() || request.getUri().endsWith("/")) {
            request.setUri("/index.html");
        }
        String filePath = webRoot + request.getUri();
        File file = new File(filePath);
        manager.log("Attempting to access url : " + file.getAbsolutePath());

        if (!file.exists()) {
            System.out.println("File does not exist: " + file.getAbsolutePath());
            response.sendError(404, "Not Found");
            return;
        }

        if (!file.canRead()) {
            System.out.println("Cannot read file: " + file.getAbsolutePath());
            response.sendError(403, "Forbidden");
            return;
        }

        byte[] content = Files.readAllBytes(file.toPath());
        response.sendResponse(200, "OK", contentType, content);
    }
}
