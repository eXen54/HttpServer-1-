package socket.server.database;

import driver.CustomDriver;
import socket.server.HttpServerManager;
import socket.server.Server;
import socket.server.clients.ClientRequest;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;


public class SgbdServer extends Server {
    private final int port;
    private ServerSocket serverSocket;
    private volatile boolean running = false;

    private final ArrayList<ClientRequest> clientRequests;
    public final HttpServerManager manager;

    public Connection connection;
    private static String localDb = "jdbc:customdb:Etudiants";
    private static final String sqlUrl = "jdbc:mysql://localhost:3306/network_project";

//    private Connection inisializeMySqlConnection() {
//        Connection result = null;
//        try {
//            result = DriverManager.getConnection(sqlUrl, "root", "");
//        } catch (SQLException s) {
//            manager.log(s.getMessage());
//        }
//        return result;
//    }
    private Connection inisializeMySqlConnection() {
        try {
            DriverManager.registerDriver(new CustomDriver());

            DriverManager.drivers().forEach(driver -> System.out.println(driver.getClass().getName()));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        Connection result = null;
        String url = "jdbc:customdb:Etudiants";
        // Attempt to connect
        try (Connection connection = DriverManager.getConnection(url)) {
            result = connection;
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
        return result;
    }

    private Connection inisializeCustomConnection() {
        return null;
    }

    public SgbdServer(int port, HttpServerManager manager) throws SQLException {
        super(ServerType.SGBD, port, manager);
        this.port = port;
        this.manager = manager;
        this.clientRequests = new ArrayList<>();
        this.connection = inisializeMySqlConnection();

    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;
        manager.log("SGBD Server started on port " + port);

        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();

                ClientRequest request = new ClientRequest(clientSocket, "", this, manager);

                clientRequests.add(request);
                manager.log(" Client connected to SGBD ");

                request.allowThis();

            } catch (IOException e) {
                if (!running) break;
                manager.log("Server error: " + e.getMessage());
            }
        }
        clientRequests.clear();
    }

    public void stop() throws Exception {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            manager.log("Error stopping server: " + e.getMessage());
        }
    }

    public void removeClientRequest(ClientRequest request) {
        clientRequests.remove(request);
    }
}
