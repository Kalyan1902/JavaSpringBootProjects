package com.filemanager.fm.Service;

import java.time.LocalDateTime;

public class FileMetadata {

    private String name;
    private long size;
    private String formattedSize;
    private String type;
    private LocalDateTime uploadDate;
    private Long userId;
    private String userEmail;

    // --- Constructors ---
    public FileMetadata() {
        // default constructor
    }

    public FileMetadata(String name, long size, String type,
                        LocalDateTime uploadDate, Long userId) {
        this.name = name;
        this.size = size;
        this.formattedSize = formatSize(size); // auto-generate readable size
        this.type = type;
        this.uploadDate = uploadDate;
        this.userId = userId;
    }

    // constructor including email
    public FileMetadata(String name, long size, String type,
                        LocalDateTime uploadDate, Long userId, String userEmail) {
        this.name = name;
        this.size = size;
        this.formattedSize = formatSize(size);
        this.type = type;
        this.uploadDate = uploadDate;
        this.userId = userId;
        this.userEmail = userEmail;
    }

    // --- Getters and Setters ---
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getSize() { return size; }
    public void setSize(long size) {
        this.size = size;
        this.formattedSize = formatSize(size); // update formatted size too
    }

    public String getFormattedSize() { return formattedSize; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    // --- Utility method to format size ---
    private String formatSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return (size / 1024) + " KB";
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }

    // --- toString for debugging ---
    @Override
    public String toString() {
        return "FileMetadata{" +
                "name='" + name + '\'' +
                ", size=" + size +
                " (" + formattedSize + ")" +
                ", type='" + type + '\'' +
                ", uploadDate=" + uploadDate +
                ", userId=" + userId +
                ", userEmail='" + userEmail + '\'' +
                '}';
    }
}
