
package com.cts.vis.controller;

import com.cts.vis.service.ClaimService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/approvals")
public class ApprovalController {

    private final ClaimService claimService;

    // View all claims with status 'SUBMITTED'
    @GetMapping
    public String approvals(Model model) {
        model.addAttribute("claims", claimService.submittedClaims());
        return "admin/approvals";
    }

    @PostMapping("/{claimId}/approve")
    public String approve(@PathVariable Long claimId) {
        // If claimId doesn't exist, service throws NotFoundException.
        // GlobalExceptionHandler intercepts and redirects with error message.
        claimService.approve(claimId);
        return "redirect:/admin/approvals?approved=true";
    }

    @PostMapping("/{claimId}/reject")
    public String reject(@PathVariable Long claimId) {
        // Business logic or data integrity errors are handled globally.
        claimService.reject(claimId);
        return "redirect:/admin/approvals?rejected=true";
    }
}