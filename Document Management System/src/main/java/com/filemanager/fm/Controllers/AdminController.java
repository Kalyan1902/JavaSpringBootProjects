package com.filemanager.fm.Controllers;

import com.filemanager.fm.Entities.UserEntity;
import com.filemanager.fm.Repositories.UserRepository;
import com.filemanager.fm.Service.FileStorageService;
import com.filemanager.fm.Service.FileMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public AdminController(UserRepository userRepository, FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/home")
    public String adminHome() {
        return "redirect:/admin/all-files";
    }

    // ✅ Move file to another user
    @PostMapping("/change-user")
    public String changeUser(@RequestParam Long currentUserId,
                             @RequestParam Long newUserId,
                             @RequestParam String fileName,
                             RedirectAttributes redirectAttributes) throws IOException {
        UserEntity currentUser = userRepository.findById(currentUserId).orElseThrow();
        UserEntity newUser = userRepository.findById(newUserId).orElseThrow();

        fileStorageService.moveFile(fileName, currentUser.getEmail(), newUser.getEmail());

        redirectAttributes.addFlashAttribute("message", "File moved successfully!");
        return "redirect:/admin/all-files";
    }

    @GetMapping("/all-files")
    public String allFiles(@RequestParam(value="searchTerm", required=false) String searchTerm,
                           Model model) throws IOException {
        Map<UserEntity, List<FileMetadata>> filesByUser = new LinkedHashMap<>();

        for (UserEntity user : userRepository.findAll()) {
            List<FileMetadata> userFiles = fileStorageService.listFileMetadata(user.getEmail(), user.getId());
            userFiles.forEach(f -> f.setUserEmail(user.getEmail()));

            if (searchTerm != null && !searchTerm.isBlank()) {
                userFiles = userFiles.stream()
                        .filter(f -> f.getName().toLowerCase().contains(searchTerm.toLowerCase()))
                        .toList();
            }

            filesByUser.put(user, userFiles);
        }

        model.addAttribute("allUsers", userRepository.findAll());
        model.addAttribute("filesByUser", filesByUser);
        model.addAttribute("adminMode", true);
        model.addAttribute("searchTerm", searchTerm);

        return "list_files";
    }

    @GetMapping("/view/{userId}/{fileName}")
    public ResponseEntity<Resource> viewFile(@PathVariable Long userId,
                                             @PathVariable String fileName) throws IOException {
        UserEntity user = userRepository.findById(userId).orElseThrow();
        File file = fileStorageService.getDownloadFile(fileName, user.getEmail());

        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + fileName);
        }

        String mimeType = Files.probeContentType(file.toPath());
        if (mimeType == null) mimeType = "application/octet-stream";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, mimeType)
                .body(new FileSystemResource(file));
    }

    @GetMapping("/download/{userId}/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long userId,
                                                 @PathVariable String fileName,
                                                 RedirectAttributes redirectAttributes) throws IOException {
        UserEntity user = userRepository.findById(userId).orElseThrow();
        File file = fileStorageService.getDownloadFile(fileName, user.getEmail());

        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + fileName);
        }

        Resource resource = new FileSystemResource(file);
        redirectAttributes.addFlashAttribute("message", "File downloaded successfully!");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .body(resource);
    }

    @PostMapping("/delete/{userId}/{fileName}")
    public String deleteFile(@PathVariable Long userId,
                             @PathVariable String fileName,
                             RedirectAttributes redirectAttributes) throws IOException {
        UserEntity user = userRepository.findById(userId).orElseThrow();
        fileStorageService.deleteFile(fileName, user.getEmail());
        redirectAttributes.addFlashAttribute("message", "File deleted successfully!");
        return "redirect:/admin/all-files";
    }

    @PostMapping("/rename-file")
    public String renameFile(@RequestParam Long userId,
                             @RequestParam String oldName,
                             @RequestParam String newName,
                             RedirectAttributes redirectAttributes) throws IOException {
        UserEntity user = userRepository.findById(userId).orElseThrow();

        // ✅ Duplicate check before renaming
        if (fileStorageService.fileExists(newName, user.getEmail())) {
            redirectAttributes.addFlashAttribute("error",
                    "A file with the name '" + newName + "' already exists. Please choose a different name.");
            return "redirect:/admin/all-files";
        }

        fileStorageService.renameFile(oldName, newName, user.getEmail());
        redirectAttributes.addFlashAttribute("message", "File renamed successfully!");
        return "redirect:/admin/all-files";
    }

}
