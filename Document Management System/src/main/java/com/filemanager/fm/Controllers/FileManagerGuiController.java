package com.filemanager.fm.Controllers;

import com.filemanager.fm.Entities.UserEntity;
import com.filemanager.fm.Repositories.UserRepository;
import com.filemanager.fm.Service.FileMetadata;
import com.filemanager.fm.Service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
public class FileManagerGuiController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UserRepository userRepository;

    // Show uploader page
    @GetMapping("/uploader")
    public String uploader() {
        return "uploader";
    }

    // List files (user vs admin, optional search)
    @GetMapping("/list-files")
    public String listFiles(@RequestParam(value = "searchTerm", required = false) String searchTerm,
                            Model model,
                            Authentication auth) throws IOException {
        String email = auth.getName(); // login credential
        Optional<UserEntity> optionalUser = userRepository.findByEmail(email);
        String displayName = optionalUser.map(UserEntity::getName).orElse(email);

        List<FileMetadata> files;
        if (auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            // Admin: list all users' files with metadata
            files = userRepository.findAll().stream()
                    .flatMap(user -> {
                        try {
                            return fileStorageService.listFileMetadata(user.getEmail(), user.getId()).stream();
                        } catch (IOException e) {
                            return List.<FileMetadata>of().stream();
                        }
                    })
                    .filter(f -> searchTerm == null || f.getName().toLowerCase().contains(searchTerm.toLowerCase()))
                    .toList();
            model.addAttribute("adminMode", true);
        } else {
            // Normal user: list own files with metadata
            files = fileStorageService.listFileMetadata(email);
            if (searchTerm != null && !searchTerm.isBlank()) {
                files = files.stream()
                        .filter(f -> f.getName().toLowerCase().contains(searchTerm.toLowerCase()))
                        .toList();
            }
            model.addAttribute("adminMode", false);
        }

        model.addAttribute("files", files);
        model.addAttribute("username", displayName);

        return "list_files";
    }

    // Upload single file
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file,
                             RedirectAttributes redirectAttributes,
                             Authentication auth) {
        try {
            fileStorageService.saveFile(file, auth.getName());
            redirectAttributes.addFlashAttribute("message", "File uploaded successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Upload failed: " + e.getMessage());
        }
        return "redirect:/list-files";
    }

    // Upload multiple files
    @PostMapping("/upload-multiple")
    public String uploadMultiple(@RequestParam("files") MultipartFile[] files,
                                 RedirectAttributes redirectAttributes,
                                 Authentication auth) {
        try {
            for (MultipartFile file : files) {
                fileStorageService.saveFile(file, auth.getName());
            }
            redirectAttributes.addFlashAttribute("message", "Files uploaded successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Upload failed: " + e.getMessage());
        }
        return "redirect:/list-files";
    }

    @GetMapping("/rename")
    public String showRenamePage(@RequestParam("fileName") String fileName, Model model) {
        model.addAttribute("fileName", fileName);
        return "rename_file";
    }

    @PostMapping("/rename")
    public String renameFile(@RequestParam("oldName") String oldName,
                             @RequestParam("newName") String newName,
                             RedirectAttributes redirectAttributes,
                             Authentication auth) {
        try {
            fileStorageService.renameFile(oldName, newName, auth.getName());
            redirectAttributes.addFlashAttribute("message", "File renamed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Rename failed: " + e.getMessage());
        }
        return "redirect:/list-files";
    }

    @GetMapping("/delete-file")
    public String deleteFile(@RequestParam("fileName") String fileName,
                             RedirectAttributes redirectAttributes,
                             Authentication auth) {
        try {
            fileStorageService.deleteFile(fileName, auth.getName());
            redirectAttributes.addFlashAttribute("message", "File deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Delete failed: " + e.getMessage());
        }
        return "redirect:/list-files";
    }
}
