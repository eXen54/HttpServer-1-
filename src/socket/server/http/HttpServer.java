package socket.server.http;

import socket.server.HttpServerManager;
import socket.server.Server;
import socket.server.clients.ClientRequest;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer extends Server {
    private ServerSocket serverSocket;
    private volatile boolean running = false;
    private final ArrayList<SubDir> subDirs;
    private final ArrayList<ClientRequest> clientRequests;
    private final ExecutorService executorService;
    private final int numberOfSubDirs;

    public HttpServer(int port, String webRoots, HttpServerManager manager, int numberOfSubDirs) {
        super(ServerType.HTTP, port, webRoots, manager);
        this.clientRequests = new ArrayList<>();
        this.numberOfSubDirs = numberOfSubDirs;
        this.subDirs = initializeSubRoots();
        this.executorService = Executors.newCachedThreadPool();
    }

    private ArrayList<SubDir> initializeSubRoots() {
        ArrayList<SubDir> roots = new ArrayList<>();
        String webRoot = getWebRoot().orElse("");

        // Create root directories if they don't exist
        for (int i = 1; i <= numberOfSubDirs; i++) {
            String rootName = "root" + i;
            File rootDir = new File(webRoot, rootName);
            if (!rootDir.exists()) {
                rootDir.mkdirs();
            }
            roots.add(new SubDir(webRoot, rootName));
        }
        return roots;
    }

    public void initializeFromHtDoc(String htDocPath) throws IOException {
        File htDocDir = new File(htDocPath);
        if (!htDocDir.exists() || !htDocDir.isDirectory()) {
            throw new IOException("Invalid htDoc directory: " + htDocPath);
        }

        // Clear and reinitialize all root directories
        for (SubDir subDir : subDirs) {
            File rootDir = new File(getWebRoot().orElse(""), subDir.getPath());
            if (rootDir.exists()) {
                deleteDirectoryContents(rootDir);
            } else {
                rootDir.mkdirs();
            }

            // Copy contents from htDoc to each root directory
            copyDirectoryContents(htDocDir, rootDir);
        }
    }

    private void deleteDirectoryContents(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectoryContents(file);
                }
                file.delete();
            }
        }
    }

    private void copyDirectoryContents(File source, File destination) throws IOException {
        if (!destination.exists()) {
            destination.mkdirs();
        }

        File[] files = source.listFiles();
        if (files != null) {
            for (File file : files) {
                File destFile = new File(destination, file.getName());
                if (file.isDirectory()) {
                    copyDirectoryContents(file, destFile);
                } else {
                    Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    public void start() throws IOException {
        HttpServerManager manager = getManager();
        serverSocket = new ServerSocket(getPort());
        running = true;
        manager.log("HTTP Server started on port " + getPort());
        manager.updateSubDirTable(subDirs);

        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                SubDir selectedSubDir = findLeastOccupiedSubDir();
                String path = selectedSubDir.getFullPath();
                ClientRequest request = new ClientRequest(
                        clientSocket,
                        path,
                        this,
                        manager
                );

                selectedSubDir.incrementClientCount();
                clientRequests.add(request);

                manager.log("Client connected to " + selectedSubDir.getPath());
                manager.updateSubDirTable(subDirs);
                manager.updateClientTable(request, "Connected to " + selectedSubDir.getPath());

                request.allowThis();

            } catch (IOException e) {
                if (!running) break;
                manager.log("Server error: " + e.getMessage());
            }
        }
        clientRequests.clear();
    }

    private SubDir findLeastOccupiedSubDir() {
        return subDirs.stream()
                .min(Comparator.comparingInt(SubDir::getNumberOfClientsConnected))
                .orElse(subDirs.get(0));
    }

    public void stop() throws Exception {
        HttpServerManager manager = getManager();
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
            executorService.shutdown();
        } catch (IOException e) {
            manager.log("Error stopping server: " + e.getMessage());
        }
    }

    public void freeSubRoot(String subDirPath) {
        HttpServerManager manager = getManager();
        subDirs.stream()
                .filter(subDir -> subDir.getPath().equals(subDirPath))
                .findFirst()
                .ifPresent(subDir -> {
                    subDir.decrementClientCount();
                    manager.updateSubDirTable(subDirs);
                });
    }

    public void removeClientRequest(ClientRequest request) {
        clientRequests.remove(request);
    }
}