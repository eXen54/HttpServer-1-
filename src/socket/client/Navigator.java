package socket.client;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.*;
import javax.swing.text.html.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Navigator extends JFrame {
    private JEditorPane displayPane;
    private JTextField urlField;
    private JButton backButton, forwardButton, refreshButton, homeButton;
    private WebBrowserModel model;
    private BrowserCache browserCache;
    private JLabel statusBar;
    private JMenu bookmarksMenu, cacheMenu;
    private JMenuBar menuBar;
    private static final String HOME_PAGE = "http://localhost:8080/index.html";
    private StyleSheet styleSheet;
    private URL baseURL;
    private HTMLEditorKit htmlKit;

    public Navigator() {
        super("Enhanced Web Browser");
        this.model = new WebBrowserModel();
        this.browserCache = new BrowserCache();
        this.styleSheet = new StyleSheet();
        this.htmlKit = new CustomHTMLEditorKit();
        try {
            this.baseURL = new URL(HOME_PAGE);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        // Set up main window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 720);

        // Create components
        JPanel toolBar = createToolBar();
        displayPane = createDisplayPane();
        JScrollPane scrollPane = new JScrollPane(displayPane);
        statusBar = createStatusBar();

        // Layout
        setLayout(new BorderLayout());
        add(toolBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);

        // Create menus
        createMenuBar();

        // Set default styles
        setupDefaultStyles();

        // Initial load of home page
        loadURL(HOME_PAGE);
    }

    private void setupDefaultStyles() {
        styleSheet = ((HTMLDocument) displayPane.getDocument()).getStyleSheet();

        // Add default styles
        styleSheet.addRule("body { font-family: Arial, sans-serif; margin: 0; padding: 8px; }");
        styleSheet.addRule("a { color: #0066cc; text-decoration: none; }");
        styleSheet.addRule("a:hover { text-decoration: underline; }");
        styleSheet.addRule("img { max-width: 100%; height: auto; }");
        styleSheet.addRule("pre { background-color: #f5f5f5; padding: 10px; border-radius: 4px; }");
        styleSheet.addRule("code { font-family: monospace; }");
    }

    private void handleLoadError(Exception e, String urlText) {
        String errorPage = String.format("""
                <html>
                <body style='font-family: Arial, sans-serif; padding: 20px;'>
                    <h2 style='color: #d32f2f;'>Page Load Error</h2>
                    <p>Failed to load: %s</p>
                    <p>Error: %s</p>
                    <hr>
                    <p style='color: #666;'>Try checking your internet connection or reloading the page.</p>
                </body>
                </html>
                """, urlText, e.getMessage());

        displayPane.setText(errorPage);
        updateStatusBar("Error loading page: " + e.getMessage());
    }

    private JEditorPane createDisplayPane() {
        JEditorPane pane = new JEditorPane();
        pane.setEditable(false);
        pane.setContentType("text/html");
        pane.setEditorKit(htmlKit);

        // Enable better HTML rendering
        pane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        pane.putClientProperty(JEditorPane.W3C_LENGTH_UNITS, Boolean.TRUE);

        // Enhanced hyperlink listener
        pane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
                pane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                updateStatusBar(e.getURL() != null ? e.getURL().toString() : "");
            } else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
                pane.setCursor(Cursor.getDefaultCursor());
                updateStatusBar("");
            } else if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                handleHyperlinkActivation(e);
            }
        });

        return pane;
    }

    private void handleHyperlinkActivation(HyperlinkEvent e) {
        if (e instanceof HTMLFrameHyperlinkEvent) {
            HTMLFrameHyperlinkEvent frameEvent = (HTMLFrameHyperlinkEvent) e;
            HTMLDocument doc = (HTMLDocument) displayPane.getDocument();
            doc.processHTMLFrameHyperlinkEvent(frameEvent);
            return;
        }

        try {
            String href = e.getDescription();
            URL url;

            if (href != null) {
                if (href.startsWith("javascript:")) {
                    handleJavaScriptLink(href);
                    return;
                }

                // Resolve relative URLs against base URL
                url = new URL(baseURL, href);
                String urlStr = url.toString();

                // Add to history before loading
                model.addToHistory(urlStr, getPageTitle(displayPane.getDocument()));
                loadURL(urlStr);
            }
        } catch (Exception ex) {
            updateStatusBar("Error loading link: " + ex.getMessage());
        }
    }

    private class CustomHTMLEditorKit extends HTMLEditorKit {
        @Override
        public Document createDefaultDocument() {
            HTMLDocument doc = (HTMLDocument) super.createDefaultDocument();
            doc.setAsynchronousLoadPriority(-1); // Synchronous loading
            if (baseURL != null) {
                doc.setBase(baseURL);
            }
            return doc;
        }

        @Override
        public ViewFactory getViewFactory() {
            return new HTMLFactory() {
                @Override
                public View create(Element elem) {
                    Object o = elem.getAttributes().getAttribute(StyleConstants.NameAttribute);
                    if (o instanceof HTML.Tag) {
                        HTML.Tag kind = (HTML.Tag) o;
                        if (kind == HTML.Tag.INPUT || kind == HTML.Tag.SELECT ||
                            kind == HTML.Tag.TEXTAREA) {
                            return new FormElementView(elem);
                        }
                    }
                    return super.create(elem);
                }
            };
        }
    }


    private class FormElementView extends ComponentView {
        private JComponent component;

        public FormElementView(Element elem) {
            super(elem);
            createComponent();
        }

        @Override
        protected Component createComponent() {
            AttributeSet attrs = getElement().getAttributes();
            String type = (String) attrs.getAttribute(HTML.Attribute.TYPE);
            String name = (String) attrs.getAttribute(HTML.Attribute.NAME);
            String value = (String) attrs.getAttribute(HTML.Attribute.VALUE);

            if (type == null || type.equals("text")) {
                JTextField field = new JTextField(15);
                if (value != null) field.setText(value);
                component = field;
            } else if (type.equals("submit")) {
                JButton button = new JButton(value != null ? value : "Submit");
                button.addActionListener(e -> {
                    Element formElem = findFormElement();
                    if (formElem != null) {
                        handleFormSubmit(formElem);
                    }
                });
                component = button;
            } else if (type.equals("password")) {
                JPasswordField field = new JPasswordField(15);
                if (value != null) field.setText(value);
                component = field;
            } else if (type.equals("checkbox")) {
                JCheckBox checkBox = new JCheckBox();
                checkBox.setSelected("checked".equals(attrs.getAttribute(HTML.Attribute.CHECKED)));
                component = checkBox;
            } else if (type.equals("radio")) {
                JRadioButton radio = new JRadioButton();
                radio.setSelected("checked".equals(attrs.getAttribute(HTML.Attribute.CHECKED)));
                component = radio;
            } else if (type.equals("hidden")) {
                JTextField hidden = new JTextField(value);
                hidden.setVisible(false);
                component = hidden;
            } else {
                component = new JTextField(15);
            }

            return component;
        }

        private Map<String, String> collectFormData(Element formElement) {
            Map<String, String> data = new HashMap<>();
            ElementIterator iterator = new ElementIterator(formElement);
            Element elem;

            while ((elem = iterator.next()) != null) {
                AttributeSet attrs = elem.getAttributes();
                Object tagObject = attrs.getAttribute(StyleConstants.NameAttribute);

                if (tagObject instanceof HTML.Tag) {
                    HTML.Tag tag = (HTML.Tag) tagObject;
                    if (tag == HTML.Tag.INPUT || tag == HTML.Tag.TEXTAREA || tag == HTML.Tag.SELECT) {
                        String name = (String) attrs.getAttribute(HTML.Attribute.NAME);
                        if (name != null) {
                            // Find all views for this element
                            View view = displayPane.getUI().getRootView(displayPane).getView(0);
                            Component comp = findComponentForElement(view, elem);

                            if (comp != null) {
                                String value = getComponentValue(comp, attrs);
                                if (value != null) {
                                    data.put(name, value);
                                }
                            }
                        }
                    }
                }
            }
            return data;
        }

        private Component findComponentForElement(View view, Element targetElem) {
            if (view instanceof FormElementView && view.getElement() == targetElem) {
                return ((FormElementView) view).component;
            }

            for (int i = 0; i < view.getViewCount(); i++) {
                View child = view.getView(i);
                Component found = findComponentForElement(child, targetElem);
                if (found != null) {
                    return found;
                }
            }
            return null;
        }

        private String getComponentValue(Component comp, AttributeSet attrs) {
            if (comp instanceof JTextField) {
                return ((JTextField) comp).getText();
            } else if (comp instanceof JPasswordField) {
                return new String(((JPasswordField) comp).getPassword());
            } else if (comp instanceof JTextArea) {
                return ((JTextArea) comp).getText();
            } else if (comp instanceof JComboBox) {
                Object selectedItem = ((JComboBox<?>) comp).getSelectedItem();
                return selectedItem != null ? selectedItem.toString() : "";
            } else if (comp instanceof JCheckBox) {
                JCheckBox checkBox = (JCheckBox) comp;
                if (checkBox.isSelected()) {
                    String value = (String) attrs.getAttribute(HTML.Attribute.VALUE);
                    return value != null ? value : "on";
                }
            } else if (comp instanceof JRadioButton) {
                JRadioButton radio = (JRadioButton) comp;
                if (radio.isSelected()) {
                    String value = (String) attrs.getAttribute(HTML.Attribute.VALUE);
                    return value != null ? value : "on";
                }
            }
            return "";  // Return empty string instead of null for form fields
        }

        private Element findFormElement() {
            Element elem = getElement();
            while (elem != null && !elem.getName().equals("form")) {
                elem = elem.getParentElement();
            }
            return elem;
        }

        private void handleFormSubmit(Element formElem) {
            try {
                Map<String, String> formData = collectFormData(formElem);
                AttributeSet attrs = formElem.getAttributes();
                String action = (String) attrs.getAttribute(HTML.Attribute.ACTION);
                String method = (String) attrs.getAttribute(HTML.Attribute.METHOD);

                if (action == null) {
                    action = baseURL.toString();
                }

                URL actionUrl = new URL(baseURL, action);
                String absoluteAction = actionUrl.toString();

                if (method == null || method.isEmpty()) {
                    method = "get";
                }

                if ("post".equalsIgnoreCase(method)) {
                    submitPostForm(absoluteAction, formData);
                } else {
                    submitGetForm(absoluteAction, formData);
                }
            } catch (Exception e) {
                updateStatusBar("Error submitting form: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }


    private void submitGetForm(String action, Map<String, String> data) {
        try {
            StringBuilder urlBuilder = new StringBuilder();

            // Handle base URL
            if (!action.contains("://")) {
                urlBuilder.append(baseURL.getProtocol()).append("://")
                        .append(baseURL.getHost());
                if (baseURL.getPort() != -1) {
                    urlBuilder.append(":").append(baseURL.getPort());
                }
            }

            // Add action path
            urlBuilder.append(action);

            // Add query parameters
            if (!data.isEmpty()) {
                urlBuilder.append(action.contains("?") ? "&" : "?");
                urlBuilder.append(buildQueryString(data));
            }

            String finalUrl = urlBuilder.toString();
            System.out.println("Submitting GET request to: " + finalUrl); // Debug log

            // Add to history and load the URL
            model.addToHistory(finalUrl, getPageTitle(displayPane.getDocument()));
            loadURL(finalUrl);

        } catch (Exception e) {
            e.printStackTrace();
            handleLoadError(e, action);
        }
    }

    private void submitPostForm(String action, Map<String, String> data) {
        try {
            // Create full URL if action is relative
            URL url;
            if (!action.contains("://")) {
                url = new URL(baseURL.getProtocol() + "://" +
                              baseURL.getHost() +
                              (baseURL.getPort() != -1 ? ":" + baseURL.getPort() : "") +
                              action);
            } else {
                url = new URL(action);
            }

            // Create connection
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            // Write POST data
            String postData = buildQueryString(data);
            System.out.println("Submitting POST request to: " + url + " with data: " + postData); // Debug log

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = postData.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Handle response
            int responseCode = conn.getResponseCode();
            System.out.println("Response code: " + responseCode); // Debug log

            if (responseCode >= 300 && responseCode < 400) {
                // Handle redirect
                String redirectUrl = conn.getHeaderField("Location");
                if (redirectUrl != null) {
                    URL newUrl = new URL(url, redirectUrl);
                    model.addToHistory(action, getPageTitle(displayPane.getDocument()));
                    loadURL(newUrl.toString());
                }
            } else if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read response
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line).append("\n");
                    }

                    // Update UI with response
                    model.addToHistory(action, getPageTitle(displayPane.getDocument()));
                    loadContentWithStyles(response.toString());
                    urlField.setText(url.toString());
                    updateStatusBar("Form submitted successfully");
                }
            } else {
                // Handle error response
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorResponse.append(line).append("\n");
                    }
                    handleLoadError(new IOException("HTTP " + responseCode + ": " + errorResponse.toString()), action);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            handleLoadError(e, action);
        }
    }

    private String buildQueryString(Map<String, String> data) throws UnsupportedEncodingException {
        StringBuilder queryString = new StringBuilder();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (queryString.length() > 0) {
                queryString.append('&');
            }
            queryString.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                    .append('=')
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return queryString.toString();
    }

    private void collectFormData(Element formElem, Map<String, String> formData) {
        try {
            ElementIterator iterator = new ElementIterator(formElem);
            Element elem;

            while ((elem = iterator.next()) != null) {
                AttributeSet attrs = elem.getAttributes();
                Object tagObj = attrs.getAttribute(StyleConstants.NameAttribute);

                if (tagObj instanceof HTML.Tag) {
                    HTML.Tag tag = (HTML.Tag) tagObj;

                    if (tag == HTML.Tag.INPUT || tag == HTML.Tag.TEXTAREA || tag == HTML.Tag.SELECT) {
                        String name = (String) attrs.getAttribute(HTML.Attribute.NAME);
                        if (name != null) {
                            String type = (String) attrs.getAttribute(HTML.Attribute.TYPE);

                            // For radio buttons and checkboxes, only include if checked
                            if ("radio".equals(type) || "checkbox".equals(type)) {
                                boolean checked = "checked".equals(attrs.getAttribute(HTML.Attribute.CHECKED));
                                if (checked) {
                                    String value = (String) attrs.getAttribute(HTML.Attribute.VALUE);
                                    formData.put(name, value != null ? value : "on");
                                }
                            } else {
                                // For other input types, get the current value
                                String value = (String) attrs.getAttribute(HTML.Attribute.VALUE);
                                if (value != null) {
                                    formData.put(name, value);
                                }
                            }
                        }
                    }
                }
            }

            System.out.println("Collected form data: " + formData); // Debug log
        } catch (Exception e) {
            e.printStackTrace();
            updateStatusBar("Error collecting form data: " + e.getMessage());
        }
    }

    private String buildPostData(Map<String, String> data) throws UnsupportedEncodingException {
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (postData.length() > 0) {
                postData.append('&');
            }
            postData.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                    .append('=')
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return postData.toString();
    }

    private void handleJavaScriptLink(String javascriptUrl) {
        // Basic JavaScript link handling
        String script = javascriptUrl.substring("javascript:".length());
        if (script.startsWith("void(0)") || script.equals("void(0);")) {
            // Ignore void calls
            return;
        }
        // Handle simple alert() calls
        if (script.startsWith("alert(")) {
            String message = script.substring(6, script.length() - 1).replace("'", "").replace("\"", "");
            JOptionPane.showMessageDialog(this, message);
        }
        // Add more JavaScript handlers as needed
    }

    private class CustomHTMLEditorPane extends JEditorPane {
        public CustomHTMLEditorPane() {
            super();
            setEditorKit(new CustomHTMLEditorKit());
        }
    }

    // Custom HTMLEditorKit to handle forms

    private String getPageTitle(Document doc) {
        return Optional.ofNullable(doc.getProperty("title"))
                .map(Object::toString)
                .orElse("");
    }

    private void loadContentWithStyles(String content) {
        try {
            // Extract and apply CSS from content
            int styleStart = content.indexOf("<style>");
            int styleEnd = content.indexOf("</style>");

            if (styleStart != -1 && styleEnd != -1) {
                String cssContent = content.substring(styleStart + 7, styleEnd);
                String[] cssRules = cssContent.split("}");

                for (String rule : cssRules) {
                    if (!rule.trim().isEmpty()) {
                        styleSheet.addRule(rule + "}");
                    }
                }
            }

            // Set the document's base URL before loading content
            HTMLDocument doc = (HTMLDocument) displayPane.getDocument();
            if (baseURL != null) {
                doc.setBase(baseURL);
            }

            // Load the content
            displayPane.setText(content);

            // Scroll to top
            SwingUtilities.invokeLater(() -> {
                displayPane.setCaretPosition(0);
            });
        } catch (Exception e) {
            e.printStackTrace();
            updateStatusBar("Error loading content: " + e.getMessage());
        }
    }


    private JPanel createToolBar() {
        JPanel toolBar = new JPanel(new BorderLayout());

        // Navigation buttons panel
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backButton = createStyledNavigationButton("←", "Go Back");
        forwardButton = createStyledNavigationButton("→", "Go Forward");
        refreshButton = createStyledNavigationButton("⟳", "Refresh Page");
        homeButton = createStyledNavigationButton("⌂", "Home");

        navPanel.add(backButton);
        navPanel.add(forwardButton);
        navPanel.add(refreshButton);
        navPanel.add(homeButton);

        // URL field and Go button
        urlField = new JTextField();
        urlField.setToolTipText("Enter URL or search term");
        JButton goButton = new JButton("Go");
        goButton.setToolTipText("Navigate to URL");

        // Add components to toolbar
        toolBar.add(navPanel, BorderLayout.WEST);
        toolBar.add(urlField, BorderLayout.CENTER);
        toolBar.add(goButton, BorderLayout.EAST);

        // Add listeners
        setupListeners(goButton);

        return toolBar;
    }

    private void createMenuBar() {
        menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> {
            model.saveHistoryToFile();
            System.exit(0);
        });
        fileMenu.add(exitItem);

        // Bookmarks Menu
        bookmarksMenu = new JMenu("Bookmarks");
        bookmarksMenu.setMnemonic(KeyEvent.VK_B);

        JMenuItem addBookmarkItem = new JMenuItem("Add Bookmark");
        addBookmarkItem.addActionListener(e -> addCurrentPageToBookmarks());
        bookmarksMenu.add(addBookmarkItem);

        // Cache Menu
        cacheMenu = new JMenu("Cache");
        JMenuItem cacheCurrentPageItem = new JMenuItem("Cache Current Page");
        cacheCurrentPageItem.addActionListener(e -> cacheCurrentPage());

        JMenuItem viewCacheItem = new JMenuItem("View Cached Content");
        viewCacheItem.addActionListener(e -> showCachedContentDialog());

        JMenuItem clearCacheItem = new JMenuItem("Clear Cache");
        clearCacheItem.addActionListener(e -> {
            browserCache.clearCache();
            JOptionPane.showMessageDialog(this, "Cache cleared successfully!");
        });
        JMenu historyMenu = new JMenu("History");
        historyMenu.setMnemonic(KeyEvent.VK_H);

        JMenuItem viewHistoryItem = new JMenuItem("View History");
        viewHistoryItem.addActionListener(e -> showHistoryDialog());

        JMenuItem clearHistoryItem = new JMenuItem("Clear History");
        clearHistoryItem.addActionListener(e -> {
            model.clearHistory();
            model.saveHistoryToFile();
            JOptionPane.showMessageDialog(this, "Browser history cleared.");
        });

        historyMenu.add(viewHistoryItem);
        historyMenu.add(clearHistoryItem);

        // Add history menu to menu bar
        menuBar.add(historyMenu);

        cacheMenu.add(cacheCurrentPageItem);
        cacheMenu.add(viewCacheItem);
        cacheMenu.add(clearCacheItem);

        // Add menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(bookmarksMenu);
        menuBar.add(cacheMenu);

        // Set menu bar
        setJMenuBar(menuBar);
    }

    private void cacheCurrentPage() {
        Optional<String> currentUrl = model.getCurrentUrl();
        if (currentUrl.isPresent()) {
            String url = currentUrl.get();
            try {
                // Get the current page content from the display pane
                String content = displayPane.getText();
                String contentType = displayPane.getContentType();

                // Cache the content
                browserCache.cacheContent(url, content, contentType);

                updateStatusBar("Page cached successfully: " + url);
                JOptionPane.showMessageDialog(this, "Page cached successfully!");
            } catch (Exception e) {
                updateStatusBar("Error caching page: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Error caching page: " + e.getMessage(),
                        "Caching Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showCachedContentDialog() {
        String url = JOptionPane.showInputDialog(this, "Enter URL to view cached content:");
        if (url != null && !url.trim().isEmpty()) {
            Optional<BrowserCache.CachedContent> cachedContent = browserCache.getCachedContent(url);

            if (cachedContent.isPresent()) {
                JDialog contentDialog = new JDialog(this, "Cached Content", true);
                contentDialog.setSize(800, 600);

                JTextArea contentArea = new JTextArea(cachedContent.get().getContent());
                contentArea.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(contentArea);

                contentDialog.add(scrollPane);
                contentDialog.setLocationRelativeTo(this);
                contentDialog.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "No cached content found for the URL.",
                        "Cache Lookup", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void showHistoryDialog() {
        // Ensure history is loaded
        model.loadHistoryFromFile();

        JDialog historyDialog = new JDialog(this, "Browser History", true);
        historyDialog.setSize(600, 400);

        // Get the display list after reloading
        java.util.List<String> historyDisplayList = model.getHistoryDisplayList();
        JList<String> historyList = new JList<>(historyDisplayList.toArray(new String[0]));
        JScrollPane scrollPane = new JScrollPane(historyList);

        // Add a menu bar to the history dialog with clear history option
        JMenuBar dialogMenuBar = new JMenuBar();
        JMenu actionMenu = new JMenu("Actions");
        JMenuItem clearHistoryItem = new JMenuItem("Clear History");
        clearHistoryItem.addActionListener(e -> {
            model.clearHistory();
            model.saveHistoryToFile();
            historyDialog.dispose();
            JOptionPane.showMessageDialog(this, "Browser history cleared.");
        });
        actionMenu.add(clearHistoryItem);
        dialogMenuBar.add(actionMenu);
        historyDialog.setJMenuBar(dialogMenuBar);

        historyList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedHistory = historyList.getSelectedValue();
                if (selectedHistory != null) {
                    // Extract URL from the history entry
                    String url = selectedHistory.split(" - ", 2)[1];
                    loadURL(url);
                    historyDialog.dispose();
                }
            }
        });

        historyDialog.add(scrollPane);
        historyDialog.setLocationRelativeTo(this);
        historyDialog.setVisible(true);
    }

    private JButton createStyledNavigationButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        return button;
    }

    private JLabel createStatusBar() {
        JLabel statusBar = new JLabel("Ready");
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        return statusBar;
    }


    private void addCurrentPageToBookmarks() {
        Optional<String> currentUrl = model.getCurrentUrl();
        if (currentUrl.isPresent()) {
            String url = currentUrl.get();
            JMenuItem bookmarkItem = new JMenuItem(url);
            bookmarkItem.addActionListener(e -> loadURL(url));
            bookmarksMenu.add(bookmarkItem);
        }
    }


    private void setupListeners(JButton goButton) {
        // URL field listeners
        urlField.addActionListener(e -> loadURL(urlField.getText()));
        goButton.addActionListener(e -> loadURL(urlField.getText()));

        // Navigation button listeners
        backButton.addActionListener(e -> navigateBack());
        forwardButton.addActionListener(e -> navigateForward());
        refreshButton.addActionListener(e -> refreshPage());
        homeButton.addActionListener(e -> loadURL(HOME_PAGE));

        updateNavigationButtons();
    }

    private void loadURL(String urlText) {
        updateStatusBar("Loading " + urlText + "...");
        urlText = sanitizeURL(urlText);

        try {
            // Create URL, handling both absolute and relative URLs
            URL url;
            if (urlText.matches("^[a-zA-Z]+://.*")) {
                // Absolute URL
                url = new URL(urlText);
            } else {
                // Relative URL
                url = new URL(baseURL, urlText);
            }

            // Update baseURL for relative link resolution
            baseURL = url;

            // Check cache first
            Optional<BrowserCache.CachedContent> cachedContent = browserCache.getCachedContent(url.toString());

            if (cachedContent.isPresent()) {
                displayPane.setContentType(cachedContent.get().getContentType());
                loadContentWithStyles(cachedContent.get().getContent());
                urlField.setText(url.toString());
                updateNavigationButtons();
            } else {
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setInstanceFollowRedirects(true);
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                        StringBuilder content = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            content.append(line).append("\n");
                        }

                        // Update UI and base URL
                        String finalUrl = conn.getURL().toString();
                        baseURL = new URL(finalUrl);
                        urlField.setText(finalUrl);

                        // Set the document base and load content
                        HTMLDocument doc = (HTMLDocument) displayPane.getDocument();
                        doc.setBase(baseURL);
                        loadContentWithStyles(content.toString());

                        // Update history and navigation
                        updateNavigationButtons();
                        updateStatusBar("Done");
                    }
                } else {
                    handleLoadError(new IOException("HTTP " + responseCode), urlText);
                }
            }
        } catch (Exception e) {
            handleLoadError(e, urlText);
        }
    }


    private String sanitizeURL(String urlText) {
        // Trim whitespace
        urlText = urlText.trim();

        // Handle localhost and local server URLs
        if (urlText.startsWith("localhost") || urlText.startsWith("127.0.0.1")) {
            urlText = "http://" + urlText;
        }

        // Don't modify relative URLs
        if (urlText.contains("://") || urlText.startsWith("/")) {
            return urlText;
        }

        // Handle search queries
        if (!urlText.contains("/") && !urlText.contains(".")) {
            return "https://www.google.com/search?q=" + URLEncoder.encode(urlText, StandardCharsets.UTF_8);
        }

        // Add protocol if missing for absolute URLs
        if (!urlText.startsWith("http://") && !urlText.startsWith("https://") && !urlText.startsWith("/")) {
            urlText = "http://" + urlText;
        }

        return urlText;
    }

    private void navigateBack() {
        Optional<String> url = model.goBack();
        url.ifPresent(this::loadURL);
    }

    private void navigateForward() {
        Optional<String> url = model.goForward();
        url.ifPresent(this::loadURL);
    }

    private void refreshPage() {
        Optional<String> currentUrl = model.getCurrentUrl();
        currentUrl.ifPresent(this::loadURL);
    }

    private void updateNavigationButtons() {
        backButton.setEnabled(model.canGoBack());
        forwardButton.setEnabled(model.canGoForward());
    }

    private void updateStatusBar(String message) {
        statusBar.setText(message);
    }

    public static void main(String[] args) {
        // Set system look and feel for better integration
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            Navigator browser = new Navigator();
            browser.setVisible(true);
        });
    }
}