package com.cts.vis.config;
import com.cts.vis.model.Customer;
import com.cts.vis.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@Component
public class GlobalModelAttributes {

    private final CustomerService customerService;
    @Autowired
    public GlobalModelAttributes(CustomerService customerService) {
        this.customerService = customerService;
    }

    @ModelAttribute
    public void addGlobal(Model model, Authentication authentication) {
        if (authentication != null) {
            model.addAttribute("authEmail", authentication.getName());
        }

        try {
            Customer c = customerService.getCurrentCustomer();
            model.addAttribute("customerName", c.getName());
        } catch (Exception ignored) {
        }
    }
}