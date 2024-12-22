package socket.client;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class WebBrowserModel {
    // Nested class to store more information about each navigation entry
    private static class HistoryEntry {
        private final String url;
        private final LocalDateTime timestamp;
        private final String title;

        public HistoryEntry(String url, String title) {
            this.url = url;
            this.title = title;
            this.timestamp = LocalDateTime.now();
        }

        @Override
        public String toString() {
            return String.format("[%s] %s - %s",
                    timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    title,
                    url
            );
        }

        public String getUrl() {
            return url;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public String getTitle() {
            return title;
        }
    }

    private final ArrayList<HistoryEntry> history;
    private int currentIndex;
    private static final int MAX_HISTORY_SIZE = 500;
    private static final String HISTORY_FILE = "src/socket/client/history/browser_history.txt";

    public WebBrowserModel() {
        history = new ArrayList<>();
        currentIndex = -1;
        loadHistoryFromFile();
    }

    /**
     * Add a new URL to the navigation history
     * @param url The URL to add
     * @param title The title of the page (optional)
     */
    public void addToHistory(String url, String title) {
        // Remove any forward history
        if (currentIndex < history.size() - 1) {
            history.subList(currentIndex + 1, history.size()).clear();
        }

        // Add new entry
        HistoryEntry newEntry = new HistoryEntry(url, title);
        history.add(newEntry);
        currentIndex = history.size() - 1;

        // Trim history if it exceeds max size
        if (history.size() > MAX_HISTORY_SIZE) {
            history.remove(0);
            currentIndex--;
        }
    }
    public void saveHistoryToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(HISTORY_FILE))) {
            for (HistoryEntry entry : history) {
                writer.println(entry.toString());
            }
        } catch (IOException e) {
            System.err.println("Error saving history: " + e.getMessage());
        }
    }

    /**
     * Navigate back in history
     * @return Optional containing the previous URL, or empty if no previous URL exists
     */
    public Optional<String> goBack() {
        if (canGoBack()) {
            currentIndex--;
            return Optional.of(history.get(currentIndex).getUrl());
        }
        return Optional.empty();
    }
    public void loadHistoryFromFile() {
        File historyFile = new File(HISTORY_FILE);
        if (!historyFile.exists()) {
            try {
                historyFile.createNewFile();
            } catch (IOException e) {
                System.err.println("Error creating history file: " + e.getMessage());
            }
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(historyFile))) {
            history.clear();
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    // More robust parsing
                    int firstBracketEnd = line.indexOf("]");
                    int secondBracketEnd = line.indexOf(" - ", firstBracketEnd);

                    if (firstBracketEnd != -1 && secondBracketEnd != -1) {
                        String timestampPart = line.substring(1, firstBracketEnd);
                        String title = line.substring(firstBracketEnd + 2, secondBracketEnd);
                        String url = line.substring(secondBracketEnd + 3);

                        HistoryEntry entry = new HistoryEntry(url, title);
                        history.add(entry);
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing history entry: " + line);
                }
            }
            currentIndex = history.isEmpty() ? -1 : history.size() - 1;
        } catch (IOException e) {
            System.err.println("Error loading history: " + e.getMessage());
        }
    }

    /**
     * Navigate forward in history
     * @return Optional containing the next URL, or empty if no forward URL exists
     */
    public Optional<String> goForward() {
        if (canGoForward()) {
            currentIndex++;
            return Optional.of(history.get(currentIndex).getUrl());
        }
        return Optional.empty();
    }

    /**
     * Check if navigation back is possible
     * @return true if can go back, false otherwise
     */
    public boolean canGoBack() {
        return currentIndex > 0;
    }

    /**
     * Check if navigation forward is possible
     * @return true if can go forward, false otherwise
     */
    public boolean canGoForward() {
        return currentIndex < history.size() - 1;
    }

    /**
     * Get the current URL
     * @return Optional containing the current URL, or empty if no history exists
     */
    public Optional<String> getCurrentUrl() {
        return currentIndex >= 0
                ? Optional.of(history.get(currentIndex).getUrl())
                : Optional.empty();
    }

    /**
     * Get the full browsing history
     * @return Unmodifiable list of URLs in history
     */
    public List<String> getFullHistory() {
        return history.stream()
                .map(HistoryEntry::getUrl)
                .collect(Collectors.toList());
    }

    /**
     * Get detailed history with timestamps and titles
     * @return Unmodifiable list of history entries
     */
    public List<HistoryEntry> getDetailedHistory() {
        return Collections.unmodifiableList(history);
    }

    /**
     * Clear the entire browsing history
     */
    public void clearHistory() {
        history.clear();
        currentIndex = -1;
    }

    /**
     * Remove a specific URL from history
     * @param url URL to remove
     * @return true if URL was found and removed, false otherwise
     */
    public boolean removeFromHistory(String url) {
        for (int i = 0; i < history.size(); i++) {
            if (history.get(i).getUrl().equals(url)) {
                history.remove(i);
                // Adjust current index if needed
                if (i <= currentIndex) {
                    currentIndex--;
                }
                return true;
            }
        }
        return false;
    }
    public List<String> getHistoryDisplayList() {
        return history.stream()
                .map(HistoryEntry::toString)
                .collect(Collectors.toList());
    }

    /**
     * Get the number of entries in the history
     * @return Number of history entries
     */
    public int getHistorySize() {
        return history.size();
    }
}