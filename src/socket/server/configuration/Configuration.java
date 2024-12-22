package socket.server.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Configuration {
    private int httpPort;
    private int sgbdPort;
    private String apikey;
    private String uploadDirFile;
    private int numberSubdir;
    private String htDoc;
    private String webRoot;

    // Getters and Setters
    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    public int getSgbdPort() {
        return sgbdPort;
    }

    public void setSgbdPort(int sgbdPort) {
        this.sgbdPort = sgbdPort;
    }

    public String getApikey() {
        return apikey;
    }

    public void setApikey(String apikey) {
        this.apikey = apikey;
    }

    public String getUploadDirFile() {
        return uploadDirFile;
    }

    public void setUploadDirFile(String uploadDirFile) {
        this.uploadDirFile = uploadDirFile;
    }

    public int getNumberSubdir() {
        return numberSubdir;
    }

    public void setNumberSubdir(int numberSubdir) {
        this.numberSubdir = numberSubdir;
    }

    public String getHtDoc() {
        return htDoc;
    }

    public void setHtDoc(String htDoc) {
        this.htDoc = htDoc;
    }

    public String getWebRoot() {
        return webRoot;
    }

    public void setWebRoot(String webRoot) {
        this.webRoot = webRoot;
    }

    // Load configuration from a JSON file
    public static Configuration loadConfiguration(String path) {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(path)) {
            return gson.fromJson(reader, Configuration.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Save configuration to a JSON file
    public void saveConfiguration(String path) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(path)) {
            gson.toJson(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
