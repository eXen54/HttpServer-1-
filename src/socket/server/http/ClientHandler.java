package socket.server.http;

import socket.server.HttpServerManager;
import socket.server.Server;
import socket.server.http.handler.ContentHandler;
import socket.server.http.handler.ContentHandlerFactory;
import socket.server.protocols.HttpRequest;
import socket.server.protocols.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final String webRoot;
    private HttpServerManager httpServerManager;
    private Server server;

    public ClientHandler(Server server, String webRoot, Socket clientSocket, HttpServerManager manager) {
        this.clientSocket = clientSocket;
        this.webRoot = webRoot;
        this.httpServerManager = manager;
        this.server = server;
    }

    @Override
    public void run() {
        try (InputStream input = clientSocket.getInputStream();
             OutputStream output = clientSocket.getOutputStream()) {

            HttpRequest request = HttpRequest.parse(input);
            HttpResponse response = new HttpResponse(output);

            if (request.isUnValidRequest()) {
                httpServerManager.log(" Invalid request ");
            } else {
                ContentHandler handler = ContentHandlerFactory.getHandler(this.server, request.getUri(), httpServerManager);
                handler.handle(this.server, webRoot, request, response, httpServerManager);
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                new HttpResponse(clientSocket.getOutputStream())
                        .sendError(500, "Internal Server Error");
            } catch (IOException ignored) {
            }
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ignored) {
            }
        }
    }
}
