package com.cts.vis.controller;

import com.cts.vis.dto.PolicyDTO;
import com.cts.vis.model.Policy;
import com.cts.vis.service.PolicyService;
import com.cts.vis.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/customer/policies")
public class PolicyController {

    private final PolicyService policyService;
    private final VehicleService vehicleService;

    @GetMapping
    public String policies(Model model) {
        return refreshDashboard(model, new PolicyDTO.CreateRequest());
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("policy") PolicyDTO.CreateRequest dto,
                         BindingResult result, Model model) {

        if (result.hasErrors()) {
            return refreshDashboard(model, dto);
        }

        // Service handles business logic. Errors are caught by GlobalExceptionHandler.
        policyService.createPolicy(dto);
        return "redirect:/customer/policies?created=true";
    }

    @PostMapping("/{policyId}/renew")
    public String renew(@PathVariable Long policyId) {
        // Business rule violations (like renewing an active policy)
        // throw IllegalStateException, caught globally.
        policyService.renewPolicy(policyId);
        return "redirect:/customer/policies?renewed=true";
    }

    @GetMapping("/{policyId}/edit")
    public String edit(@PathVariable Long policyId, Model model) {
        // NotFoundException caught globally if policyId is invalid.
        model.addAttribute("policy", policyService.getMyPolicy(policyId));
        return "customer/policy-edit";
    }

    @PostMapping("/{policyId}/edit")
    public String update(@PathVariable Long policyId,
                         @Valid @ModelAttribute("policy") PolicyDTO.UpdateRequest dto,
                         BindingResult result, Model model) {

        if (result.hasErrors()) {
            model.addAttribute("policy", policyService.getMyPolicy(policyId));
            return "customer/policy-edit";
        }

        policyService.updatePolicy(policyId, dto.getCoverageAmount());
        return "redirect:/customer/policies?updated=true";
    }

    private String refreshDashboard(Model model, PolicyDTO.CreateRequest form) {
        List<Policy> policies = policyService.myPolicies();
        model.addAttribute("policies", policies);
        model.addAttribute("editDisabled", policyService.getPolicyLockStatus(policies));
        model.addAttribute("vehicles", vehicleService.myVehicles());
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("policy", form);
        return "customer/policies";
    }
}