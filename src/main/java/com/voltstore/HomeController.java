package com.voltstore;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Authentication auth, Model model) {
        if (auth != null && auth.getName() != null) {
            model.addAttribute("userName", auth.getName());
        }
        return "index";
    }
}