package socket.server.http.handler;

import socket.server.HttpServerManager;
import socket.server.Server;
import socket.server.protocols.HttpRequest;
import socket.server.protocols.HttpResponse;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;

public class PhpHandler implements ContentHandler {
    @Override
    public void handle(Server server, String webRoot, HttpRequest request, HttpResponse response, HttpServerManager manager) throws IOException {
        String originalUri = request.getUri();
        String filePath;
        int indexParameter = originalUri.indexOf("?");
        if (indexParameter > 0 ) {
            filePath = webRoot + originalUri.substring(0, indexParameter);
        } else {
            filePath = webRoot + originalUri;
        }

        File file = new File(filePath);

        if (!file.exists()) {
            response.sendError(404, "Not Found");
            return;
        }

        ArrayList<String> command = new ArrayList<>();
        command.add("php");
        command.add(filePath);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Map<String, String> env = processBuilder.environment();


        if (indexParameter != -1) {
            String queryString = originalUri.substring(indexParameter + 1);
            env.put("QUERY_STRING", queryString);
        }

        // Handle different request methods
        env.put("REQUEST_METHOD", request.getMethod());

        // For POST requests, pass body data
        if ("POST".equals(request.getMethod())) {
            String postData = request.getBody();
            env.put("CONTENT_TYPE", request.getHeader("Content-Type"));
            env.put("CONTENT_LENGTH", String.valueOf(postData.length()));

            Process process = processBuilder.start();

            // Write POST data to process input stream
            try (OutputStream processInput = process.getOutputStream()) {
                processInput.write(postData.getBytes());
                processInput.flush();
            }

            // Read response
            try (InputStream processOutput = process.getInputStream()) {
                byte[] content = processOutput.readAllBytes();
                response.sendResponse(200, "OK", "text/html", content);
            }
        } else {
            // For GET or other methods
            Process process = processBuilder.start();

            // Read response
            try (InputStream processOutput = process.getInputStream()) {
                byte[] content = processOutput.readAllBytes();
                response.sendResponse(200, "OK", "text/html", content);
            }
        }
    }

}
