package socket.server;

import socket.server.clients.ClientRequest;
import socket.server.configuration.Configuration;
import socket.server.database.SgbdServer;
import socket.server.http.HttpServer;
import socket.server.http.SubDir;

import javax.swing.Timer;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HttpServerManager extends JFrame {
    private JTextField apiKeyField;
    private JButton generateApiKeyButton;
    private JButton copyApiKeyButton;
    private String currentApiKey;

    public String getApiKey() {
        return currentApiKey;
    }

    // HTTP Server Components
    private JTextField webRootField;
    public JTextField uploadDirectoryField;
    private JTextField httpPortField;

    // SGBD Server Components
    private JTextField sgbdPortField;

    // Tables and Models
    private JTable subDirTable;
    private JButton initializeSubDirButton;
    private DefaultTableModel subDirTableModel;
    private JTable clientTable;
    private DefaultTableModel clientTableModel;
    private JTextArea logArea;

    // Server Buttons
    private JButton startHttpServerButton;
    private JButton stopHttpServerButton;
    private JButton startSgbdServerButton;
    private JButton stopSgbdServerButton;
    private JButton browseWebRootButton;
    private JButton browseUploadDirButton;
    private JButton saveLogButton;
    private JButton clearLogButton;
    private JButton saveConfigButton;

    private JTextField htDocTextField;
    private JTextField numberSubDirTextField;
    private JTextField webRootTextField;
    private JButton browseHtDocButton;
    private JButton browseWebRoot;
    private JSpinner numberSubDirSpinner;



    // Server Instances
    private HttpServer httpServer;
    private SgbdServer sgbdServer;
    private Thread httpServerThread;
    private Thread sgbdServerThread;

    // Color Palette
    private static final Color BACKGROUND_COLOR = new Color(135, 206, 235); // Sky Blue
    private static final Color PANEL_BACKGROUND = new Color(173, 216, 230); // Light Sky Blue
    private static final Color TEXT_COLOR = new Color(0, 0, 0); // Black
    private static final Color BUTTON_COLOR = new Color(100, 149, 237); // Cornflower Blue

    private String htDoc;
    private int numberSubdir;
    private String webroot;
    private String uploadDir;

    private int sgbdPort;
    private int httpPort;
    private void initAdditionalComponents() {
        // HtDoc components
        htDocTextField = new JTextField(htDoc);
        browseHtDocButton = new JButton("Browse");
        browseHtDocButton.addActionListener(e -> browseDirectory(htDocTextField));

        // Number of subdirectories spinner
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(4, 1, 10, 1);  // default, min, max, step
        numberSubDirSpinner = new JSpinner(spinnerModel);
        numberSubDirSpinner.setValue(numberSubdir);

        // Add change listener to update configuration when number of subdirs changes
        numberSubDirSpinner.addChangeListener(e -> {
            numberSubdir = (Integer) numberSubDirSpinner.getValue();
            // You might want to trigger reinitialization here
        });
    }

    // Modify your createConfigPanel() method to add the new components
    private void addNewConfigComponents(JPanel configPanel, GridBagConstraints gbc) {
        // Add HtDoc field
        addLabeledField(configPanel, "HtDoc Directory:",
                createFieldWithButton(htDocTextField, browseHtDocButton), gbc, 4);

        // Add Number of Subdirectories spinner
        addLabeledField(configPanel, "Number of Subdirectories:",
                numberSubDirSpinner, gbc, 5);
    }


    public HttpServerManager() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void initAll () {
        initComponents();
        setupLayout();
        applyTheme();
    }

    private void initComponents() {
        // HTTP Port field
        httpPortField = new JTextField(String.valueOf(this.httpPort));
        sgbdPortField = new JTextField(String.valueOf(this.sgbdPort));
        webRootField = new JTextField(webroot);
        browseWebRootButton = new JButton("Browse");
        browseWebRootButton.addActionListener(e -> browseDirectory(webRootField));

        // Initialize HtDoc components
        htDocTextField = new JTextField(htDoc);
        browseHtDocButton = new JButton("Browse");
        browseHtDocButton.addActionListener(e -> browseDirectory(htDocTextField));

        // Initialize number of subdirectories spinner
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(4, 1, 10, 1);  // default, min, max, step
        numberSubDirSpinner = new JSpinner(spinnerModel);
        numberSubDirSpinner.setValue(numberSubdir);

        // Add change listener to update configuration when number of subdirs changes
        numberSubDirSpinner.addChangeListener(e -> {
            numberSubdir = (Integer) numberSubDirSpinner.getValue();
        });

        // Upload Directory components
        uploadDirectoryField = new JTextField(uploadDir);
        browseUploadDirButton = new JButton("Browse");
        browseUploadDirButton.addActionListener(e -> browseDirectory(uploadDirectoryField));

        // Server buttons
        startHttpServerButton = new JButton("Start HTTP Server");
        stopHttpServerButton = new JButton("Stop HTTP Server");
        stopHttpServerButton.setEnabled(false);

        startSgbdServerButton = new JButton("Start SGBD Server");
        stopSgbdServerButton = new JButton("Stop SGBD Server");
        stopSgbdServerButton.setEnabled(false);

        initializeSubDirButton = new JButton("Initialize Subdirectories");
        saveLogButton = new JButton("Save Log");
        clearLogButton = new JButton("Clear Log");

        copyApiKeyButton = new JButton("Copy API Key");
        copyApiKeyButton.addActionListener(e -> copyApiKeyToClipboard());

        // Client table setup
        String[] columnNames = {"Server", "Client IP", "Connection Time", "Web Root", "Status"};
        clientTableModel = new DefaultTableModel(columnNames, 0);
        clientTable = new JTable(clientTableModel);

        // Log area
        logArea = new JTextArea(10, 50);
        logArea.setEditable(false);

        // Subdirectory table setup
        String[] subDirColumns = {"Sub Directory", "Path", "Clients Connected"};
        subDirTableModel = new DefaultTableModel(subDirColumns, 0);
        subDirTable = new JTable(subDirTableModel);

        // Button actions
        startHttpServerButton.addActionListener(e -> startHttpServer());
        stopHttpServerButton.addActionListener(e -> {
            try {
                stopHttpServer();
            } catch (Exception ex) {
                log(ex.getCause().getMessage());
            }
        });

        startSgbdServerButton.addActionListener(e -> startSgbdServer());
        stopSgbdServerButton.addActionListener(e -> {
            try {
                stopSgbdServer();
            } catch (Exception ex) {
                log(ex.getCause().getMessage());
            }
        });

        initializeSubDirButton.addActionListener(e -> {
            try {
                initializeSubdirectories();
            } catch (IOException ex) {
                log("Error initializing subdirectories: " + ex.getMessage());
            }
        });

        apiKeyField = new JTextField(getApiKey());
        apiKeyField.setEditable(false);
        generateApiKeyButton = new JButton("Generate API Key");
        generateApiKeyButton.addActionListener(e -> generateApiKey());

        saveConfigButton = new JButton("Save Configuration");
        saveConfigButton.addActionListener(e -> saveConfigurationToFile());

        // Log management buttons
        saveLogButton.addActionListener(e -> saveLogToFile());
        clearLogButton.addActionListener(e -> clearLog());
    }

    private void generateApiKey() {

        SecureRandom secureRandom = new SecureRandom();
        byte[] apiKeyBytes = new byte[32]; // 256 bits
        secureRandom.nextBytes(apiKeyBytes);
        currentApiKey = Base64.getUrlEncoder().withoutPadding().encodeToString(apiKeyBytes);

        apiKeyField.setText(currentApiKey);
        log("New API Key generated: " + currentApiKey);
    }

    private void browseDirectory(JTextField targetField) {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File("."));
        chooser.setDialogTitle("Select Directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            targetField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void copyApiKeyToClipboard() {
        if (currentApiKey != null && !currentApiKey.isEmpty()) {
            // Get the system clipboard
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

            // Create a transferable string selection
            StringSelection stringSelection = new StringSelection(currentApiKey);

            // Set the string selection to the clipboard
            clipboard.setContents(stringSelection, null);
            // Optional: Show a small popup or log a message
            log("API Key copied to clipboard");

            // Optional: Show a temporary tooltip
            copyApiKeyButton.setText("Copied!");
            Timer resetTimer = new Timer(2000, e -> copyApiKeyButton.setText("Copy API Key"));
            resetTimer.setRepeats(false);
            resetTimer.start();
        }
    }

    private void setupLayout() {
        setTitle("Server Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Create panels for different sections
        JPanel configPanel = createConfigPanel();
        JPanel mainContentPanel = createMainContentPanel();
        JPanel logPanel = createLogPanel();

        // Add panels to the main layout
        add(configPanel, BorderLayout.NORTH);
        add(mainContentPanel, BorderLayout.CENTER);
        add(logPanel, BorderLayout.SOUTH);

        // Set window properties
        setPreferredSize(new Dimension(1200, 800));
        pack();
        setLocationRelativeTo(null);
    }


    private JPanel createConfigPanel() {
        JPanel configPanel = new JPanel(new GridBagLayout());
        configPanel.setBorder(BorderFactory.createTitledBorder("Server Configuration"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Add basic components
        addLabeledField(configPanel, "HTTP Port:", httpPortField, gbc, 0);
        addLabeledField(configPanel, "SGBD Port:", sgbdPortField, gbc, 1);
        addLabeledField(configPanel, "Web Root:", createFieldWithButton(webRootField, browseWebRootButton), gbc, 2);
        addLabeledField(configPanel, "Upload Directory:", createFieldWithButton(uploadDirectoryField, browseUploadDirButton), gbc, 3);

        // Add HtDoc and SubDir components
        addLabeledField(configPanel, "HtDoc Directory:", createFieldWithButton(htDocTextField, browseHtDocButton), gbc, 4);
        addLabeledField(configPanel, "Number of Subdirectories:", numberSubDirSpinner, gbc, 5);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.add(startHttpServerButton);
        buttonPanel.add(stopHttpServerButton);
        buttonPanel.add(startSgbdServerButton);
        buttonPanel.add(stopSgbdServerButton);
        buttonPanel.add(initializeSubDirButton);
        buttonPanel.add(saveLogButton);
        buttonPanel.add(clearLogButton);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        configPanel.add(buttonPanel, gbc);

        // API Key Panel
        addLabeledField(configPanel, "API Key:", createApiKeyPanel(), gbc, 7);

        // Save Configuration Button
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        configPanel.add(saveConfigButton, gbc);

        return configPanel;
    }

    private JPanel createMainContentPanel() {
        JPanel mainContentPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        // Client Connections Panel
        JPanel clientPanel = new JPanel(new BorderLayout());
        clientPanel.setBorder(BorderFactory.createTitledBorder("Client Connections"));
        clientPanel.add(new JScrollPane(clientTable), BorderLayout.CENTER);

        // Sub Directories Panel
        JPanel subDirPanel = new JPanel(new BorderLayout());
        subDirPanel.setBorder(BorderFactory.createTitledBorder("Sub Directories"));
        subDirPanel.add(new JScrollPane(subDirTable), BorderLayout.CENTER);

        // Add panels to the main content panel
        mainContentPanel.add(clientPanel);
        mainContentPanel.add(subDirPanel);

        return mainContentPanel;
    }

    private JPanel createLogPanel() {
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("Server Logs"));
        logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        return logPanel;
    }

    private JPanel createApiKeyPanel() {
        JPanel apiKeyPanel = new JPanel(new BorderLayout());
        apiKeyPanel.add(apiKeyField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttonPanel.add(generateApiKeyButton);
        buttonPanel.add(copyApiKeyButton);

        apiKeyPanel.add(buttonPanel, BorderLayout.EAST);
        return apiKeyPanel;
    }

    private JPanel createFieldWithButton(JTextField field, JButton button) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(field, BorderLayout.CENTER);
        panel.add(button, BorderLayout.EAST);
        return panel;
    }

    private void addLabeledField(JPanel panel, String label, JComponent component, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        panel.add(component, gbc);
    }

    private void saveConfigurationToFile() {
        String configFilePath = "src/socket/server/configuration/config.json";
        Configuration config = new Configuration();

        try {
            config.setHttpPort(Integer.parseInt(httpPortField.getText()));
            config.setSgbdPort(Integer.parseInt(sgbdPortField.getText()));
            config.setApikey(getApiKey());
            config.setUploadDirFile(uploadDirectoryField.getText());
            config.setHtDoc(htDocTextField.getText());
            config.setNumberSubdir((Integer) numberSubDirSpinner.getValue());
            config.setWebRoot(webRootField.getText());

            // Save the configuration
            config.saveConfiguration(configFilePath);
            JOptionPane.showMessageDialog(this,
                    "Configuration saved to " + configFilePath,
                    "Save Successful",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Invalid port numbers",
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyTheme() {
        // Apply sky blue theme to components
        getContentPane().setBackground(BACKGROUND_COLOR);

        // Style buttons
        JButton[] buttons = {
                startHttpServerButton, stopHttpServerButton,
                startSgbdServerButton, stopSgbdServerButton,
                initializeSubDirButton, saveLogButton, clearLogButton
        };
        for (JButton button : buttons) {
            button.setBackground(Color.WHITE);
            button.setForeground(TEXT_COLOR);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setFont(new Font("Arial", Font.BOLD, 12));
        }

        // Style tables
        JTable[] tables = {clientTable, subDirTable};
        for (JTable table : tables) {
            table.setBackground(Color.WHITE);
            table.setSelectionBackground(new Color(173, 216, 230));
            JTableHeader header = table.getTableHeader();
            header.setBackground(PANEL_BACKGROUND);
            header.setFont(new Font("Arial", Font.BOLD, 12));
        }
        if (copyApiKeyButton != null) {
            copyApiKeyButton.setBackground(Color.WHITE);
            copyApiKeyButton.setForeground(TEXT_COLOR);
            copyApiKeyButton.setFocusPainted(false);
            copyApiKeyButton.setBorderPainted(false);
            copyApiKeyButton.setFont(new Font("Arial", Font.BOLD, 12));
        }
        // Style text components
        httpPortField.setBackground(Color.WHITE);
        sgbdPortField.setBackground(Color.WHITE);
        webRootField.setBackground(Color.WHITE);
        logArea.setBackground(Color.WHITE);
    }

    private void saveLogToFile() {
        try {
            // Create a file with timestamp in the filename
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File logFile = new File("src/socket/server/http/logs/server_log_" + timestamp + ".txt");

            try (FileWriter writer = new FileWriter(logFile)) {
                writer.write(logArea.getText());
            }

            JOptionPane.showMessageDialog(this,
                    "Log saved to " + logFile.getAbsolutePath(),
                    "Log Saved",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error saving log: " + ex.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearLog() {
        logArea.setText("");
    }

    private void initializeSubdirectories() throws IOException {
        String dataDirPath = htDoc;
        File dataDir = new File(dataDirPath);

        if (!dataDir.exists() || !dataDir.isDirectory()) {
            log("Invalid ht Doc directory : " + htDoc);
            return;
        }

        File[] contents = dataDir.listFiles();
        if (contents == null || contents.length == 0) {
            log("No files or directories found in htDoc : " + htDoc);
            return;
        }

        // Find existing root directories (up to root4)
        ArrayList<File> existingRootDirs = findExistingRootDirectories(webroot);
        if (existingRootDirs.size() > 4) {
            existingRootDirs = new ArrayList<>(existingRootDirs.subList(0, 4));
        }

        if (existingRootDirs.isEmpty()) {
            log("No root directories (root1 to root4) found.");
            return;
        }

        // Remove all contents from the existing root directories
        for (File rootDir : existingRootDirs) {
            deleteDirectoryContents(rootDir);
            log("Cleared contents of " + rootDir.getAbsolutePath());
        }

        // Copy all files and directories from /data into each root directory
        for (File rootDir : existingRootDirs) {
            for (File source : contents) {
                File destination = new File(rootDir, source.getName());
                copyDirectory(source, destination);
                log("Copied " + source.getName() + " to " + rootDir.getAbsolutePath());
            }
        }
    }

    private ArrayList<File> findExistingRootDirectories(String webRoot) {
        ArrayList<File> rootDirs = new ArrayList<>();
        File baseDir = new File(webRoot);

        File[] possibleRootDirs = baseDir.listFiles(file ->
                file.isDirectory() && file.getName().matches("root\\d+")
        );

        if (possibleRootDirs != null) {
            Arrays.sort(possibleRootDirs, Comparator.comparing(f -> {
                String name = f.getName();
                return Integer.parseInt(name.substring(4));
            }));
            rootDirs.addAll(Arrays.asList(possibleRootDirs));
        }

        return rootDirs;
    }

    private void copyDirectory(File source, File destination) throws IOException {
        if (source.isDirectory()) {
            if (!destination.exists()) {
                destination.mkdirs();
            }
            String[] children = source.list();
            if (children != null) {
                for (String child : children) {
                    copyDirectory(new File(source, child), new File(destination, child));
                }
            }
        } else {
            Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
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

    public void updateSubDirTable(ArrayList<SubDir> subDirs) {
        SwingUtilities.invokeLater(() -> {
            // Clear existing rows
            subDirTableModel.setRowCount(0);

            // Add new rows
            for (SubDir subDir : subDirs) {
                Vector<String> row = new Vector<>();
                row.add(subDir.getPath());
                row.add(subDir.getFullPath());
                row.add(String.valueOf(subDir.getNumberOfClientsConnected()));
                subDirTableModel.addRow(row);
            }
        });
    }
    private void validateDirectories(String webRoot, String uploadDir, String htDoc) {
        // Validate web root
        File webRootDir = new File(webRoot);
        if (!webRootDir.exists() && !webRootDir.mkdirs()) {
            throw new RuntimeException("Cannot create web root directory");
        }

        // Validate upload directory
        File uploadDirectory = new File(uploadDir);
        if (!uploadDirectory.exists() && !uploadDirectory.mkdirs()) {
            throw new RuntimeException("Cannot create upload directory");
        }

        // Validate htDoc directory
        File htDocDirectory = new File(htDoc);
        if (!htDocDirectory.exists() || !htDocDirectory.isDirectory()) {
            throw new RuntimeException("Invalid htDoc directory");
        }
    }

    private void startHttpServer() {
        try {
            int port = Integer.parseInt(httpPortField.getText());
            String uploadDir = uploadDirectoryField.getText();
            int numSubDirs = (Integer) numberSubDirSpinner.getValue();

            // Validate directories
            validateDirectories(this.webroot, uploadDir, htDocTextField.getText());

            // Create HTTP server with number of subdirectories
            httpServer = new HttpServer(port, this.webroot, this, numSubDirs);
            // Initialize subdirectories from htDoc
            try {
                httpServer.initializeFromHtDoc(htDocTextField.getText());
            } catch (IOException e) {
                throw new RuntimeException("Failed to initialize subdirectories: " + e.getMessage());
            }

            // Start server thread
            httpServerThread = new Thread(() -> {
                try {
                    httpServer.start();
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(this,
                                    "HTTP Server Error: " + e.getMessage(),
                                    "Server Error",
                                    JOptionPane.ERROR_MESSAGE)
                    );
                }
            });
            httpServerThread.start();

            // Update UI
            startHttpServerButton.setEnabled(false);
            stopHttpServerButton.setEnabled(true);
            log("HTTP Server started on port " + port +
                " with " + numSubDirs + " subdirectories");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error starting server: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startSgbdServer() {
        try {
            int port = Integer.parseInt(sgbdPortField.getText());

            sgbdServer = new SgbdServer(port, this);
            sgbdServerThread = new Thread(() -> {
                try {
                    sgbdServer.start();
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(this,
                                    "SGBD Server Error: " + e.getMessage(),
                                    "Server Error",
                                    JOptionPane.ERROR_MESSAGE)
                    );
                }
            });
            sgbdServerThread.start();

            startSgbdServerButton.setEnabled(false);
            stopSgbdServerButton.setEnabled(true);
            log("SGBD Server started on port " + port);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid SGBD port number", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Connection Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void stopHttpServer() throws Exception {
        if (httpServer != null) {
            httpServer.stop();
            httpServerThread.interrupt();

            startHttpServerButton.setEnabled(true);
            stopHttpServerButton.setEnabled(false);
            log("HTTP Server stopped");
            clearClientTable();
        }
    }

    private void stopSgbdServer() throws Exception {
        if (sgbdServer != null) {
            sgbdServer.stop();
            sgbdServerThread.interrupt();

            startSgbdServerButton.setEnabled(true);
            stopSgbdServerButton.setEnabled(false);
            log("SGBD Server stopped");
            clearClientTable();
        }
    }

    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public void updateClientTable(ClientRequest request, String status) {
        SwingUtilities.invokeLater(() -> {
            Vector<String> row = new Vector<>();
            row.add(request.getServer().getType().name());
            row.add(request.getSocket().getInetAddress().getHostAddress());
            row.add(LocalDateTime.now().toString());
            row.add(request.getWebRoot());
            row.add(status);
            clientTableModel.addRow(row);
        });
    }

    private void clearClientTable() {
        SwingUtilities.invokeLater(() -> {
            clientTableModel.setRowCount(0);
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String configFilePath = "src/socket/server/configuration/config.json";
            // Load configuration
            Configuration config = Configuration.loadConfiguration(configFilePath);
            if (config == null) {
                System.err.println("Failed to load configuration. Exiting...");
                return;
            }

            // Initialize HttpServerManager with loaded configuration
            HttpServerManager manager = new HttpServerManager();
            manager.setHttpPort(config.getHttpPort());
            manager.setSgbdPort(config.getSgbdPort());
            manager.setApiKey(config.getApikey());
            manager.setUploadDir(config.getUploadDirFile());
            manager.setHtDoc(config.getHtDoc());
            manager.setNumberSubdir(config.getNumberSubdir());
            manager.setWebroot(config.getWebRoot());

            manager.initAll();


            // Display the manager UI
            manager.setVisible(true);
        });
    }

    // Add these setter methods to HttpServerManager
    public void setHttpPort(int port) {
        this.httpPort = port;
    }

    public void setSgbdPort(int port) {
        this.sgbdPort = port;
    }

    public void setApiKey(String apiKey) {
        currentApiKey = apiKey;
    }

    public String getHtDoc() {
        return htDoc;
    }

    public void setHtDoc(String htDoc) {
        this.htDoc = htDoc;
    }

    public int getNumberSubdir() {
        return numberSubdir;
    }

    public void setNumberSubdir(int numberSubdir) {
        this.numberSubdir = numberSubdir;
    }

    public String getWebroot() {
        return webroot;
    }

    public void setWebroot(String webroot) {
        this.webroot = webroot;
    }

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public int getSgbdPort() {
        return sgbdPort;
    }

    public int getHttpPort() {
        return httpPort;
    }
}