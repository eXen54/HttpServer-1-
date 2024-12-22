package socket.client;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class BrowserCache {
    private static final String CACHE_DIRECTORY = "src/socket/client/cache";
    private static final long MAX_CACHE_SIZE_MB = 500; // Maximum cache size in MB
    private static final int MAX_CACHE_ENTRIES = 1000; // Maximum number of cache entries

    // Stores metadata about cached items
    private Map<String, CacheEntry> cacheIndex;

    public BrowserCache() {
        // Create cache directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(CACHE_DIRECTORY));
        } catch (IOException e) {
            System.err.println("Failed to create cache directory: " + e.getMessage());
        }

        // Load existing cache index
        cacheIndex = loadCacheIndex();
    }

    // Generate a unique filename for a URL
    private String generateCacheKey(String url) {
        try {
            // Use MD5 hash of URL to create a unique, filesystem-safe filename
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(url.getBytes());

            // Convert to hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte hashByte : hashBytes) {
                String hex = Integer.toHexString(0xff & hashByte);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // Fallback to a simple URL-based filename
            return url.replaceAll("[^a-zA-Z0-9.-]", "_");
        }
    }

    // Cache the content of a page
    public void cacheContent(String url, String content, String contentType) {
        // Check and manage cache size
        manageCacheSize();

        // Generate unique cache key
        String cacheKey = generateCacheKey(url);

        try {
            // Create cache file
            Path cacheFilePath = Paths.get(CACHE_DIRECTORY, cacheKey);
            Files.write(cacheFilePath, content.getBytes());

            // Store cache metadata
            CacheEntry entry = new CacheEntry(url, cacheKey, contentType);
            cacheIndex.put(cacheKey, entry);

            // Save updated cache index
            saveCacheIndex();
        } catch (IOException e) {
            System.err.println("Failed to cache content: " + e.getMessage());
        }
    }

    // Retrieve cached content
    public Optional<CachedContent> getCachedContent(String url) {
        // Find matching cache entry
        Optional<CacheEntry> matchingEntry = cacheIndex.values().stream()
                .filter(entry -> entry.getOriginalUrl().equals(url))
                .findFirst();

        if (matchingEntry.isPresent()) {
            try {
                CacheEntry entry = matchingEntry.get();
                Path cacheFilePath = Paths.get(CACHE_DIRECTORY, entry.getCacheKey());

                // Read cached content
                String cachedContent = new String(Files.readAllBytes(cacheFilePath));

                // Update last accessed time
                entry.updateLastAccessed();
                saveCacheIndex();

                return Optional.of(new CachedContent(cachedContent, entry.getContentType()));
            } catch (IOException e) {
                System.err.println("Error reading cached content: " + e.getMessage());
            }
        }

        return Optional.empty();
    }

    // Manage cache size by removing least recently used entries
    private void manageCacheSize() {
        // Remove entries if cache exceeds maximum size or entry count
        if (cacheIndex.size() > MAX_CACHE_ENTRIES) {
            // Sort by last accessed time and remove oldest entries
            List<String> keysToRemove = cacheIndex.entrySet().stream()
                    .sorted(Comparator.comparing(e -> e.getValue().getLastAccessed()))
                    .limit(cacheIndex.size() - MAX_CACHE_ENTRIES)
                    .map(Map.Entry::getKey)
                    .toList();

            // Remove cache files and index entries
            keysToRemove.forEach(key -> {
                try {
                    Files.deleteIfExists(Paths.get(CACHE_DIRECTORY, key));
                    cacheIndex.remove(key);
                } catch (IOException e) {
                    System.err.println("Failed to remove cache entry: " + e.getMessage());
                }
            });
        }
    }

    // Save cache index to a file
    private void saveCacheIndex() {
        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(Paths.get(CACHE_DIRECTORY, "cache_index.dat").toFile()))) {
            out.writeObject(cacheIndex);
        } catch (IOException e) {
            System.err.println("Failed to save cache index: " + e.getMessage());
        }
    }

    // Load cache index from file
    @SuppressWarnings("unchecked")
    private Map<String, CacheEntry> loadCacheIndex() {
        Path indexPath = Paths.get(CACHE_DIRECTORY, "cache_index.dat");

        if (Files.exists(indexPath)) {
            try (ObjectInputStream in = new ObjectInputStream(
                    new FileInputStream(indexPath.toFile()))) {
                return (Map<String, CacheEntry>) in.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Failed to load cache index: " + e.getMessage());
            }
        }

        // Return new cache index if loading fails
        return new ConcurrentHashMap<>();
    }

    // Clear entire cache
    public void clearCache() {
        try {
            // Delete all files in cache directory
            Files.walk(Paths.get(CACHE_DIRECTORY))
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            System.err.println("Failed to delete cache file: " + e.getMessage());
                        }
                    });

            // Reset cache index
            cacheIndex.clear();
            saveCacheIndex();
        } catch (IOException e) {
            System.err.println("Failed to clear cache: " + e.getMessage());
        }
    }

    // Nested classes for cache management
    public static class CacheEntry implements Serializable {
        private String originalUrl;
        private String cacheKey;
        private String contentType;
        private LocalDateTime lastAccessed;

        public CacheEntry(String originalUrl, String cacheKey, String contentType) {
            this.originalUrl = originalUrl;
            this.cacheKey = cacheKey;
            this.contentType = contentType;
            this.lastAccessed = LocalDateTime.now();
        }

        public void updateLastAccessed() {
            this.lastAccessed = LocalDateTime.now();
        }

        // Getters
        public String getOriginalUrl() { return originalUrl; }
        public String getCacheKey() { return cacheKey; }
        public String getContentType() { return contentType; }
        public LocalDateTime getLastAccessed() { return lastAccessed; }
    }

    public static class CachedContent {
        private final String content;
        private final String contentType;

        public CachedContent(String content, String contentType) {
            this.content = content;
            this.contentType = contentType;
        }

        public String getContent() { return content; }
        public String getContentType() { return contentType; }
    }
}