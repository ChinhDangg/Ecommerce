package dev.ecommercefrontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomeController {

    @GetMapping
    public String getHomePage() {
        return "/product-page/home-page";
    }

    @GetMapping("/login")
    public String getLoginPage() {
        return "/other-page/login";
    }
}
