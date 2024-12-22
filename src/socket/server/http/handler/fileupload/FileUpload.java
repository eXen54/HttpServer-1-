package socket.server.http.handler.fileupload;

public  class FileUpload {
    private final String originalFilename;
    private final String savedFilename;

    public FileUpload(String originalFilename, String savedFilename) {
        this.originalFilename = originalFilename;
        this.savedFilename = savedFilename;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getSavedFilename() {
        return savedFilename;
    }
}