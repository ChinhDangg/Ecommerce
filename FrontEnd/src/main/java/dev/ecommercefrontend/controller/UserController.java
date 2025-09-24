package dev.ecommercefrontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    @GetMapping("/account/address")
    public String getAccountAddressPage() {
        return "/user-page/edit-address-page";
    }

    @GetMapping("/product/review")
    public String getProductReviewPage(@RequestParam Long pid) {
        return "/user-page/product-review-page";
    }
}
