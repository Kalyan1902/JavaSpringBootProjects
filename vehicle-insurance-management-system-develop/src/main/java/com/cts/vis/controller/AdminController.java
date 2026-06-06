package com.cts.vis.controller;
import com.cts.vis.repository.CustomerRepository;
import com.cts.vis.repository.PolicyRepository;
import com.cts.vis.repository.VehicleRepository;
import com.cts.vis.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
@RequiredArgsConstructor
public class AdminController {
    private final ReportService reportService;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final PolicyRepository policyRepository;
    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        model.addAllAttributes(reportService.adminDashboardStats());
        return "admin/dashboard";
    }
    @GetMapping("/admin/customers")
    public String customers(Model model) {
        model.addAttribute("customers", customerRepository.findAll());
        return "admin/customers";
    }
    @GetMapping("/admin/vehicles")
    public String vehicles(Model model) {
        model.addAttribute("vehicles", vehicleRepository.findAll());
        return "admin/vehicles";
    }
    @GetMapping("/admin/policies")
    public String policies(Model model) {
        model.addAttribute("policies", policyRepository.findAll());
        return "admin/policies";
    }
}