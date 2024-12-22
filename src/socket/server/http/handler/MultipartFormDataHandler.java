package socket.server.http.handler;

import socket.server.HttpServerManager;
import socket.server.Server;
import socket.server.http.handler.fileupload.FileUpload;
import socket.server.http.handler.fileupload.FileUploadException;
import socket.server.protocols.HttpRequest;
import socket.server.protocols.HttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultipartFormDataHandler implements ContentHandler {
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB max file size
    private static final String[] ALLOWED_FILE_TYPES = {
            ".txt", ".pdf", ".doc", ".docx", ".jpg", ".jpeg", ".png", ".gif",
            ".mp3", ".mp4", ".zip", ".rar", ".csv", ".xls", ".xlsx", ".svg", ".html", ".json", ".css", ".js", ".php", ".java"
    };

    @Override
    public void handle(Server server, String webRoot, HttpRequest request, HttpResponse response, HttpServerManager manager) throws IOException {
        System.out.println(request.toString());
        // Validate request method
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            response.sendError(405, "Method Not Allowed");
            return;
        }


        // Check content type
        String contentType = request.getHeader("Content-Type");
        if (contentType == null || !contentType.toLowerCase().contains("multipart/form-data")) {
            response.sendError(400, "Invalid Content Type");
            return;
        }

        // Extract boundary
        String boundary = extractBoundary(contentType);
        if (boundary == null) {
            response.sendError(400, "Invalid Boundary");
            return;
        }

        // Prepare upload directory
        Path uploadDir = Paths.get(manager.uploadDirectoryField.getText());
        Files.createDirectories(uploadDir);

        try {
            ArrayList<FileUpload> uploadedFiles = parseMultipartFormData(request, boundary, uploadDir);

            // Generate response
            StringBuilder responseBody = new StringBuilder();
            responseBody.append("Upload successful:\n");
            for (FileUpload file : uploadedFiles) {
                responseBody.append(file.getOriginalFilename())
                        .append(" -> ")
                        .append(file.getSavedFilename())
                        .append("\n");
                manager.log("Attempting to upload url : " + file.getSavedFilename());

            }

            response.sendResponse(200, "OK", "text/plain",
                    responseBody.toString().getBytes(StandardCharsets.UTF_8));

        } catch (FileUploadException e) {
            response.sendError(400, e.getMessage());
        }
    }

    private String extractFilenameFromHeaders(String headers) {
        // Convert headers to lowercase for case-insensitive matching
        String lowercaseHeaders = headers.toLowerCase();

        // Different regex patterns to match filename in various formats
        String[] patterns = {
                "filename=\"([^\"]+)\"",       // Standard "filename="example.jpg""
                "filename=([^;\r\n]+)",         // filename=example.jpg
                "filename *= *\"?([^\";\r\n]+)\"?" // Handles spaces around = and optional quotes
        };

        for (String patternStr : patterns) {
            Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(headers);

            if (matcher.find()) {
                String filename = matcher.group(1).trim();

                // Additional cleanup
                filename = filename.replace("\"", "").trim();

                return filename;
            }
        }

        return null;
    }

    private ArrayList<FileUpload> parseMultipartFormData(HttpRequest request, String boundary, Path uploadDir)
            throws IOException, FileUploadException {

        ArrayList<FileUpload> uploadedFiles = new ArrayList<>();
        byte [] bodyBytes = request.getBody().getBytes();

        if (bodyBytes.length == 0) {
            throw new FileUploadException("Empty request body");
        }

        String bodyString = request.getBody();
        String[] parts = bodyString.split("--" + boundary);

        for (String part : parts) {
            if (part.trim().isEmpty() || part.startsWith("--")) continue;

            try {
                // Find the start of file content
                int headerEnd = part.indexOf("\r\n\r\n");
                if (headerEnd == -1) continue;

                // Extract headers
                String headers = part.substring(0, headerEnd);

                // Extract filename from headers
                String filename = extractFilenameFromHeaders(headers);
                if (filename == null) continue;

                // Validate file type
                if (!isAllowedFileType(filename)) {
                    System.err.println("Skipping file with disallowed type: " + filename);
                    continue;
                }

                // Extract file content
                byte[] fileContent = extractFileContent(bodyBytes, boundary, part);
                if (fileContent == null || fileContent.length == 0) continue;

                // Validate file size
                if (fileContent.length > MAX_FILE_SIZE) {
                    throw new FileUploadException("File too large: " + filename +
                                                  " (max size: " + MAX_FILE_SIZE / (1024 * 1024) + "MB)");
                }

                // Generate unique filename
                String savedFilename = generateUniqueFilename(uploadDir, filename);
                Path filePath = uploadDir.resolve(savedFilename);

                // Write file content
                Files.write(filePath, fileContent, StandardOpenOption.CREATE_NEW);

                uploadedFiles.add(new FileUpload(filename, savedFilename));

            } catch (Exception e) {
                // Log or handle individual file upload errors
                System.err.println("Error processing file part: " + e.getMessage());
            }
        }

        if (uploadedFiles.isEmpty()) {
            throw new FileUploadException("No valid files were uploaded");
        }

        return uploadedFiles;
    }

    private boolean isAllowedFileType(String filename) {
        if (filename == null) return false;

        String lowercaseFilename = filename.toLowerCase();
        for (String allowedType : ALLOWED_FILE_TYPES) {
            if (lowercaseFilename.endsWith(allowedType)) {
                return true;
            }
        }
        return false;
    }


    private byte[] extractFileContent(byte[] bodyBytes, String boundary, String part) {
        // Find the start of the actual file content
        int contentStart = part.indexOf("\r\n\r\n");
        if (contentStart == -1) return null;

        // Extract full content between boundaries
        int boundaryStart = part.indexOf("--" + boundary, contentStart);
        if (boundaryStart == -1) boundaryStart = part.length();

        // Extract the actual byte content directly from the original bodyBytes
        try {
            // Find the content in the original byte array
            int index = indexOf(bodyBytes, part.substring(contentStart + 4, boundaryStart).trim().getBytes(StandardCharsets.UTF_8));
            if (index == -1) return null;

            int contentEnd = index + (boundaryStart - contentStart - 4);
            return Arrays.copyOfRange(bodyBytes, index, contentEnd);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Helper method to find byte array within another byte array
    private int indexOf(byte[] outerArray, byte[] innerArray) {
        for (int i = 0; i <= outerArray.length - innerArray.length; i++) {
            boolean found = true;
            for (int j = 0; j < innerArray.length; j++) {
                if (outerArray[i + j] != innerArray[j]) {
                    found = false;
                    break;
                }
            }
            if (found) return i;
        }
        return -1;
    }
    private String extractBoundary(String contentType) {
        int boundaryIndex = contentType.indexOf("boundary=");
        return boundaryIndex == -1
                ? null
                : contentType.substring(boundaryIndex + 9);
    }

    private String generateUniqueFilename(Path uploadDir, String originalFilename) {
        String baseName = originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");
        String extension = "";
        int dotIndex = baseName.lastIndexOf('.');
        if (dotIndex != -1) {
            extension = baseName.substring(dotIndex);
            baseName = baseName.substring(0, dotIndex);
        }

        Path filePath;
        String newFilename;
        int counter = 0;
        do {
            newFilename = baseName + (counter > 0 ? "_" + counter : "") + extension;
            filePath = uploadDir.resolve(newFilename);
            counter++;
        } while (Files.exists(filePath));

        return newFilename;
    }
}