
package com.cts.vis.controller;

import com.cts.vis.dto.ClaimDTO;
import com.cts.vis.service.ClaimService;
import com.cts.vis.service.PolicyService;
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
public class ClaimController {

    private final ClaimService claimService;
    private final PolicyService policyService;

    @GetMapping("/customer/claims")
    public String claims(Model model) {
        // Initializes the page with a fresh DTO
        return refreshClaimsPage(model, new ClaimDTO.FileRequest());
    }

    @PostMapping("/customer/claims/file")
    public String file(@Valid @ModelAttribute("claim") ClaimDTO.FileRequest dto,
                       BindingResult result, Model model) {

        if (result.hasErrors()) {
            return refreshClaimsPage(model, dto);
        }
        try {

            claimService.fileClaim(dto);
        }
        catch(IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("claimErrorMessage",ex.getMessage());
            return refreshClaimsPage(model,dto);
        }
        return "redirect:/customer/claims?submitted=true";
    }

    private String refreshClaimsPage(Model model, ClaimDTO.FileRequest form) {
        model.addAttribute("policies", policyService.myPolicies());
        model.addAttribute("claims", claimService.myClaims());
        model.addAttribute("claim", form);
        return "customer/claims";
    }
}