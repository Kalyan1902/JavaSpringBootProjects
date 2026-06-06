package com.cts.vis.controller;

import com.cts.vis.dto.CustomerDTO;
import com.cts.vis.model.Customer;
import com.cts.vis.service.CustomerService;
import com.cts.vis.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/customer")
public class CustomerController {

    private final CustomerService customerService;
    private final ReportService reportService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Customer c = customerService.getCurrentCustomer();
        model.addAttribute("customer", c);

        // reportService handles the statistics gathering
        model.addAllAttributes(reportService.customerDashboardStats());
        return "customer/dashboard";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        model.addAttribute("customer", customerService.getCurrentCustomer());

        // Pre-fill the form with current data from the service layer
        model.addAttribute("profile", customerService.getProfileUpdateDto());
        return "customer/profile";
    }

    @PostMapping("/profile")
    public String update(@ModelAttribute("profile") CustomerDTO.ProfileUpdateRequest dto) {
        // No try-catch here.
        // Validation or persistence errors are handled by GlobalExceptionHandler.
        customerService.updateProfile(dto);

        return "redirect:/customer/profile?saved=true";
    }
}