package socket.server;

import java.util.Optional;

public abstract class Server {
    private final ServerType type;
    private int port;
    private HttpServerManager manager;
    private String webRoot;
    public enum ServerType {
        HTTP, SGBD
    }

    public Server(ServerType type, int port, HttpServerManager manager) {
        this.type = type;
        this.port = port;
        this.manager = manager;
    }

    public Server(ServerType type, int port, String webRoot, HttpServerManager manager) {
        this(type, port, manager);
        this.webRoot = webRoot;
    }

    public Optional<String> getWebRoot() {
        return Optional.ofNullable(webRoot);
    }

    public void setWebRoot(String webRoot) {
        this.webRoot = webRoot;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public HttpServerManager getManager() {
        return manager;
    }

    public void setManager(HttpServerManager manager) {
        this.manager = manager;
    }

    public ServerType getType() {
        return type;
    }
    public abstract void start () throws Exception;
}