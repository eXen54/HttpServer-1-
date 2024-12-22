package socket.server.http.handler;

import socket.server.HttpServerManager;
import socket.server.Server;
import socket.server.protocols.HttpRequest;
import socket.server.protocols.HttpResponse;

import java.io.IOException;
import java.sql.SQLException;

public interface ContentHandler {
    void handle(Server server, String webRoot, HttpRequest request, HttpResponse response, HttpServerManager manager) throws Exception;
}
