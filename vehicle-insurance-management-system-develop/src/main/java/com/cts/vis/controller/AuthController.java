package com.cts.vis.controller;

import com.cts.vis.dto.CustomerDTO;
import com.cts.vis.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/")
    public String root() {
        return "redirect:/customer/login";
    }

    /* ================= LOGIN ROUTES ================= */

    @GetMapping("/customer/login")
    public String customerLogin() {
        return "customer/login";
    }

    @GetMapping("/admin/login")
    public String adminLogin() {
        return "admin/login";
    }

    /* ================= REGISTRATION ================= */

    @GetMapping("/customer/register")
    public String registerForm(Model model) {
        model.addAttribute("customer", new CustomerDTO.RegisterRequest());
        return "customer/register";
    }

    @PostMapping("/customer/register")
    public String register(@Valid @ModelAttribute("customer") CustomerDTO.RegisterRequest dto,
                           BindingResult result) {

        // 1. Validates annotations like @NotBlank, @Email, @Size in the DTO
        if (result.hasErrors()) {
            return "customer/register";
        }

        // 2. Happy Path: Service handles logic.
        // If email exists, service throws BadRequestException.
        // The GlobalExceptionHandler will now catch it and redirect.
        authService.registerCustomer(dto);

        return "redirect:/customer/login?registered=true";
    }
}