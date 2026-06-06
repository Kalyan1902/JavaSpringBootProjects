package com.filemanager.fm.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileStorageService {

    public static final String STORAGE_DIRECTORY = "C:\\DemoStorage"; // adjust path

    public void saveFile(MultipartFile file, String username) throws IOException {
        Path userFolder = Path.of(STORAGE_DIRECTORY, username);
        Files.createDirectories(userFolder);

        Path targetPath = userFolder.resolve(file.getOriginalFilename());
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    public File getDownloadFile(String filename, String username) {
        return new File(STORAGE_DIRECTORY + File.separator + username, filename);
    }

    public boolean fileExists(String fileName, String userEmail) {
        File file = getDownloadFile(fileName, userEmail);
        return file.exists();
    }


    public void deleteFile(String filename, String username) throws IOException {
        Path filePath = Path.of(STORAGE_DIRECTORY, username, filename);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        } else {
            throw new FileNotFoundException("File not found: " + filename);
        }
    }

    // --- New method for admins: move file between users ---
    public void moveFile(String fileName, String fromUserEmail, String toUserEmail) throws IOException {
        Path source = Paths.get(STORAGE_DIRECTORY, fromUserEmail, fileName);
        Path target = Paths.get(STORAGE_DIRECTORY, toUserEmail, fileName);

        if (!Files.exists(source)) {
            throw new FileNotFoundException("File not found: " + fileName);
        }
        Files.createDirectories(target.getParent()); // ensure target user folder exists
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    public void renameFile(String oldName, String newName, String username) throws IOException {
        Path userFolder = Path.of(STORAGE_DIRECTORY, username);
        Path oldPath = userFolder.resolve(oldName);

        if (!Files.exists(oldPath)) {
            throw new FileNotFoundException("File not found: " + oldName);
        }

        String oldExtension = oldName.contains(".") ? oldName.substring(oldName.lastIndexOf('.')) : "";
        String newExtension = newName.contains(".") ? newName.substring(newName.lastIndexOf('.')) : "";

        if (!oldExtension.equalsIgnoreCase(newExtension) && !oldExtension.isEmpty()) {
            throw new IOException("You can't change the file type.");
        }

        if (!newName.contains(".") && !oldExtension.isEmpty()) {
            newName = newName + oldExtension;
        }

        Path newPath = userFolder.resolve(newName);

        if (Files.exists(newPath)) {
            throw new IOException("Target filename already exists: " + newName);
        }

        Files.move(oldPath, newPath);
    }

    // --- List only file names ---
    public List<String> listFiles(String username, String searchTerm) throws IOException {
        Path userFolder = Path.of(STORAGE_DIRECTORY, username);
        if (!Files.exists(userFolder)) {
            return List.of();
        }
        try (Stream<Path> paths = Files.list(userFolder)) {
            return paths
                    .map(path -> path.getFileName().toString())
                    .filter(name -> searchTerm == null || name.toLowerCase().contains(searchTerm.toLowerCase()))
                    .collect(Collectors.toList());
        }
    }

    // --- List full metadata (user flow, no userId) ---
    public List<FileMetadata> listFileMetadata(String username) throws IOException {
        return listFileMetadata(username, null);
    }

    // --- List full metadata (admin flow, with userId) ---
    public List<FileMetadata> listFileMetadata(String username, Long userId) throws IOException {
        Path userFolder = Path.of(STORAGE_DIRECTORY, username);
        List<FileMetadata> metadataList = new ArrayList<>();

        if (!Files.exists(userFolder)) {
            return metadataList;
        }

        try (Stream<Path> paths = Files.list(userFolder)) {
            for (Path file : paths.collect(Collectors.toList())) {
                BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);

                String name = file.getFileName().toString();
                long size = attrs.size();
                String type = Files.probeContentType(file);
                LocalDateTime uploadDate = LocalDateTime.ofInstant(
                        attrs.creationTime().toInstant(),
                        ZoneId.systemDefault()
                );

                metadataList.add(new FileMetadata(name, size, type, uploadDate, userId));
            }
        }
        return metadataList;
    }
}
