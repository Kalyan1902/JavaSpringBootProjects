/*package com.filemanager.fm.Controllers;

import com.filemanager.fm.Service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@RestController
public class FileManagerController {

    @Autowired
    private FileStorageService fileStorageService;

    // Upload file into the logged-in user's folder
    @PostMapping("/upload-file")
    public boolean uploadFile(@RequestParam("file") MultipartFile file,
                              Authentication auth) {
        try {
            // Pass both file and username (auth.getName() gives the logged-in user's email/username)
            fileStorageService.saveFile(file, auth.getName());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // Download file from the logged-in user's folder
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam("fileName") String filename,
                                                 Authentication auth) {
        try {
            File fileToDownload = fileStorageService.getDownloadFile(filename, auth.getName());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentLength(fileToDownload.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new FileSystemResource(fileToDownload));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // View file inline (per-user)
    @GetMapping("/view-file")
    public ResponseEntity<Resource> viewFile(@RequestParam("fileName") String filename,
                                             Authentication auth) throws Exception {
        File fileToView = fileStorageService.getDownloadFile(filename, auth.getName());

        String mimeType = Files.probeContentType(fileToView.toPath());
        if (mimeType == null) {
            mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentLength(fileToView.length())
                .contentType(MediaType.parseMediaType(mimeType))
                .body(new FileSystemResource(fileToView));
    }

    // Rename file (per-user)
    @PostMapping("/rename-file")
    public ResponseEntity<Void> renameFile(@RequestParam("oldName") String oldName,
                                           @RequestParam("newName") String newName,
                                           Authentication auth) throws IOException {
        fileStorageService.renameFile(oldName, newName, auth.getName());
        return ResponseEntity.ok().build();
    }
}*/
