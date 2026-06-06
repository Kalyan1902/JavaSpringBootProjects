package com.filemanager.fm.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String redirectToLogin() {
        return "redirect:/login";
    }

    // Removed duplicate /admin/home mapping

    @GetMapping("/user/home")
    public String userHome() {
        return "user_home"; // maps to user_home.html
    }
}
