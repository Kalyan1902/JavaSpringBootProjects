package com.filemanager.fm.Controllers;

import com.filemanager.fm.Entities.UserEntity;
import com.filemanager.fm.Repositories.UserRepository;
import com.filemanager.fm.Service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.nio.file.Files;
import java.nio.file.Path;

@Controller
public class RegistrationController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserEntity());
        return "register"; // points to register.html
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute UserEntity user, Model model) {
        // encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // force role to USER
        user.setRole("USER");
        // save user
        userRepository.save(user);

        // ✅ Create a folder for the new user
        try {
            Path userFolder = Path.of(FileStorageService.STORAGE_DIRECTORY, user.getEmail());
            Files.createDirectories(userFolder);
        } catch (Exception e) {
            // optional: log error if folder creation fails
            System.err.println("Failed to create user folder: " + e.getMessage());
        }

        // add success message to show on login page
        model.addAttribute("successMessage", "Registration successful! Please login.");

        // return login.html directly with message
        return "login";
    }
}
