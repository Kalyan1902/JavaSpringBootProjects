package com.cts.vis.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 1. Handles Resource Not Found (404 logic)
    @ExceptionHandler(NotFoundException.class)
    public String handleNotFound(NotFoundException ex, HttpServletRequest request, RedirectAttributes ra) {
        return handleRedirect(ex.getMessage(), request, ra);
    }

    // 2. Handles Business Logic errors (e.g., "Policy Expired")
    @ExceptionHandler(IllegalStateException.class)
    public String handleBusinessRules(IllegalStateException ex, HttpServletRequest request, RedirectAttributes ra) {
        return handleRedirect(ex.getMessage(), request, ra);
    }

    // 3. Handles Validation/Input errors
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleValidation(IllegalArgumentException ex, HttpServletRequest request, RedirectAttributes ra) {
        return handleRedirect(ex.getMessage(), request, ra);
    }

    // 4. Handles specific Authentication/Registration issues
    @ExceptionHandler(BadRequestException.class)
    public String handleBadRequest(BadRequestException ex, HttpServletRequest request, RedirectAttributes ra) {
        return handleRedirect(ex.getMessage(), request, ra);
    }

    // 5. Generic fallback for unexpected crashes
    @ExceptionHandler(Exception.class)
    public String handleGeneralError(Exception ex, HttpServletRequest request, RedirectAttributes ra) {
        // Log the actual exception here for debugging
        ex.printStackTrace();
        return handleRedirect("A system error occurred. Please try again later.", request, ra);
    }

    /**
     * PRIVATE HELPER: Decides where to send the user based on the URL
     */
    private String handleRedirect(String message, HttpServletRequest request, RedirectAttributes ra) {
        ra.addFlashAttribute("errorMessage", message);

        String uri = request.getRequestURI();

        // If the error happened in the Admin section, keep them in Admin
        if (uri.contains("/admin")) {
            return "redirect:/admin/dashboard";
        }

        // Default for Customers
        return "redirect:/customer/dashboard";
    }
}