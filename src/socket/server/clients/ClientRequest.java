package socket.server.clients;

import socket.server.HttpServerManager;
import socket.server.Server;
import socket.server.http.ClientHandler;
import socket.server.http.HttpServer;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ClientRequest {
    private final Socket socket;
    private final String webRoot;
    private final Server server;
    private final HttpServerManager httpServerManager;
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    public ClientRequest(Socket socket, String webRoot, Server server, HttpServerManager manager) {
        this.socket = socket;
        this.webRoot = webRoot;
        this.server = server;
        this.httpServerManager = manager;
    }

    public Socket getSocket() {
        return socket;
    }

    public String getWebRoot() {
        return webRoot;
    }

    public void allowThis() {
        executorService.submit(this::handleClientConnection);
    }

    private void handleClientConnection() {
        try {
            Thread handlerThread = new Thread(new ClientHandler(this.server, this.webRoot, socket, httpServerManager));
            handlerThread.start();

            monitorSocketConnection();
        } catch (Exception e) {
            server.getManager().log("Client session error: " + e.getMessage());
        } finally {
            cleanupConnection();
        }
    }

    private void monitorSocketConnection() throws InterruptedException {
        while (!socket.isClosed() && socket.isConnected()) {
            try {
                if (socket.getInputStream().read() == -1) {
                    break;
                }
                Thread.sleep(5000);
            } catch (IOException e) {
                break;
            }
        }
    }

    private void cleanupConnection() {
        try {
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            server.getManager().log("Error closing socket: " + e.getMessage());
        }

        if (server instanceof HttpServer httpServer) {
            String subRoot = new File(webRoot).getName();
            httpServer.freeSubRoot(subRoot);
            httpServer.removeClientRequest(this);
            httpServer.getManager().updateClientTable(this, "Disconnected from " + subRoot);
        }
    }

    public static void shutdownExecutorService() {
        try {
            executorService.shutdown();
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public Server getServer() {
        return server;
    }
}