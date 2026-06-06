package com.filemanager.fm.Controllers;

import com.filemanager.fm.Service.FileStorageService;
import com.filemanager.fm.Service.FileMetadata;
import com.filemanager.fm.Entities.UserEntity;
import com.filemanager.fm.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;

    @Autowired
    public UserController(FileStorageService fileStorageService, UserRepository userRepository) {
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
    }

    @GetMapping("/my-files")
    public String myFiles(@AuthenticationPrincipal User userDetails,
                          @RequestParam(value="impersonate", required=false) String impersonate,
                          @RequestParam(value="searchTerm", required=false) String searchTerm,
                          Model model) throws IOException {
        String loginUsername = (impersonate != null) ? impersonate : userDetails.getUsername();
        UserEntity userEntity = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String username = userEntity.getEmail();

        List<FileMetadata> files = fileStorageService.listFileMetadata(username);

        if (searchTerm != null && !searchTerm.isBlank()) {
            files = files.stream()
                    .filter(f -> f.getName().toLowerCase().contains(searchTerm.toLowerCase()))
                    .toList();
        }

        model.addAttribute("files", files);
        model.addAttribute("username", username);
        model.addAttribute("adminMode", impersonate != null);
        model.addAttribute("searchTerm", searchTerm);

        return "list_files";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file,
                             @AuthenticationPrincipal User userDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            UserEntity userEntity = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            String userEmail = userEntity.getEmail();
            String newFileName = file.getOriginalFilename();

            // ✅ Duplicate check
            if (fileStorageService.fileExists(newFileName, userEmail)) {
                redirectAttributes.addFlashAttribute("error",
                        "A file with the name '" + newFileName + "' already exists. Please choose a different name.");
                return "redirect:/user/my-files";
            }

            fileStorageService.saveFile(file, userEmail);
            redirectAttributes.addFlashAttribute("message", "File uploaded successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Upload failed: " + e.getMessage());
        }
        return "redirect:/user/my-files";
    }

    @PostMapping("/upload-multiple")
    public String uploadMultiple(@RequestParam("files") MultipartFile[] files,
                                 @AuthenticationPrincipal User userDetails,
                                 RedirectAttributes redirectAttributes) {
        try {
            UserEntity userEntity = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            String userEmail = userEntity.getEmail();

            for (MultipartFile file : files) {
                String newFileName = file.getOriginalFilename();
                // ✅ Duplicate check for each file
                if (fileStorageService.fileExists(newFileName, userEmail)) {
                    redirectAttributes.addFlashAttribute("error",
                            "A file with the name '" + newFileName + "' already exists. Please rename your file.");
                    return "redirect:/user/my-files";
                }
                fileStorageService.saveFile(file, userEmail);
            }
            redirectAttributes.addFlashAttribute("message", "Files uploaded successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Upload failed: " + e.getMessage());
        }
        return "redirect:/user/my-files";
    }

    @GetMapping("/view-file")
    public ResponseEntity<Resource> viewFile(@RequestParam String fileName,
                                             @AuthenticationPrincipal User userDetails) throws IOException {
        UserEntity userEntity = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String username = userEntity.getEmail();
        File file = fileStorageService.getDownloadFile(fileName, username);

        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + fileName);
        }

        Resource resource = new FileSystemResource(file);
        String mimeType = Files.probeContentType(file.toPath());
        if (mimeType == null) mimeType = "application/octet-stream";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, mimeType)
                .body(resource);
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam String fileName,
                                                 @AuthenticationPrincipal User userDetails,
                                                 RedirectAttributes redirectAttributes) throws IOException {
        UserEntity userEntity = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String username = userEntity.getEmail();
        File file = fileStorageService.getDownloadFile(fileName, username);

        if (!file.exists()) throw new FileNotFoundException("File not found: " + fileName);

        Resource resource = new FileSystemResource(file);
        redirectAttributes.addFlashAttribute("message", "File downloaded successfully!");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
                .body(resource);
    }

    @PostMapping("/delete-file")
    public String deleteFile(@RequestParam String fileName,
                             @AuthenticationPrincipal User userDetails,
                             RedirectAttributes redirectAttributes) throws IOException {
        UserEntity userEntity = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        fileStorageService.deleteFile(fileName, userEntity.getEmail());
        redirectAttributes.addFlashAttribute("message", "File deleted successfully!");
        return "redirect:/user/my-files";
    }

    @PostMapping("/rename-file")
    public String renameFile(@RequestParam String oldName,
                             @RequestParam String newName,
                             @AuthenticationPrincipal User userDetails,
                             RedirectAttributes redirectAttributes) throws IOException {
        UserEntity userEntity = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String userEmail = userEntity.getEmail();

        // ✅ Duplicate check before renaming
        if (fileStorageService.fileExists(newName, userEmail)) {
            redirectAttributes.addFlashAttribute("error",
                    "A file with the name '" + newName + "' already exists. Please choose a different name.");
            return "redirect:/user/my-files";
        }

        fileStorageService.renameFile(oldName, newName, userEmail);
        redirectAttributes.addFlashAttribute("message", "File renamed successfully!");
        return "redirect:/user/my-files";
    }
}
