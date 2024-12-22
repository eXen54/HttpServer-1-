package socket.server.http.handler;

import socket.server.HttpServerManager;
import socket.server.Server;
import socket.server.database.apihandler.DatabaseHandler;

public class ContentHandlerFactory {
    public static ContentHandler getHandler(Server typeServer, String uri, HttpServerManager manager) {
        if (typeServer.getType() == Server.ServerType.HTTP) {
            if (uri.endsWith("/")) return new StaticFileHandler("text/html");
            // Prioritize exact upload route
            if ("/upload".equals(uri)) {
                return new MultipartFormDataHandler();
            } else if (uri.endsWith(".php") || uri.contains(".php")) {
                manager.log(" Using : PHP Handler");
                return new PhpHandler();
            } else if (uri.endsWith(".html") || uri.endsWith(".htm")) {
                manager.log(" Using : StaticFileHandler Html");
                return new StaticFileHandler("text/html");
            } else if (uri.endsWith(".css")) {
                manager.log(" Using : StaticFileHandler Css");
                return new StaticFileHandler("text/css");
            } else if (uri.endsWith(".json")) {
                manager.log(" Using : StaticFileHandler Json");
                return new StaticFileHandler("application/json");
            } else {
                manager.log(" Using : StaticFileHandler Octet-Stream");
                return new StaticFileHandler("application/octet-stream");
            }
        } else {
            return new DatabaseHandler();
        }


    }
}