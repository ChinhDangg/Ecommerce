package dev.ecommercefrontend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/user")
public class UserController {

    @GetMapping("/checkout")
    public String getCheckoutPage() {
        return "/product-page/checkout-page";
    }
}
