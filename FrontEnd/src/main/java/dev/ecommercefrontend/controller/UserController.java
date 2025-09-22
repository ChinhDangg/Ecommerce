package dev.ecommercefrontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class UserController {

    @GetMapping("/checkout")
    public String getCheckoutPage() {
        return "/product-page/checkout-page";
    }

    @GetMapping("/account")
    public String getAccountPage() {
        return "/user-page/account-page";
    }

    @GetMapping("/account/order")
    public String getAccountOrderPage() {
        return "/user-page/your-order-page";
    }

    @GetMapping("/account/security")
    public String getAccountSecurityPage() {
        return "/user-page/edit-info-page";
    }
}
