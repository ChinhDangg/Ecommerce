package dev.ecommercefrontend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/user")
public class UserController {

    @GetMapping("/cart")
    public String getCartPage() {
        return "/product-page/cart-page";
    }

    @GetMapping("/checkout")
    public String getCheckoutPage() {
        return "/product-page/checkout-page";
    }
}
