package socket.server.http;

import java.io.File;

public class SubDir {
    private String path;
    private int numberOfClientsConnected;
    private String fullPath;

    public SubDir(String rootDir, String path) {
        this.path = path;
        this.numberOfClientsConnected = 0;
        this.fullPath = rootDir + File.separator + path;

        // Ensure directory exists
        File dir = new File(fullPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public String getPath() {
        return path;
    }

    public int getNumberOfClientsConnected() {
        return numberOfClientsConnected;
    }

    public void incrementClientCount() {
        numberOfClientsConnected++;
    }

    public void decrementClientCount() {
        if (numberOfClientsConnected > 0) {
            numberOfClientsConnected--;
        }
    }

    public String getFullPath() {
        return fullPath;
    }

    @Override
    public String toString() {
        return "SubDir{" +
               "path='" + path + '\'' +
               ", clients=" + numberOfClientsConnected +
               '}';
    }
}
