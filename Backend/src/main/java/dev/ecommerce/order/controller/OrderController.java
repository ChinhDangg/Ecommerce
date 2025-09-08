package dev.ecommerce.order.controller;

import dev.ecommerce.order.service.CheckoutService;
import dev.ecommerce.order.constant.ReserveStatus;
import dev.ecommerce.user.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final CheckoutService checkoutService;

    private Long getUserId(Authentication authentication) {
        if (authentication == null)
            return null;
        return ((SecurityUser) authentication.getPrincipal()).user().getId();
    }

    @GetMapping("/reserve")
    public ResponseEntity<String> reserve(Authentication authentication) {
        Long userId = getUserId(authentication);
        ReserveStatus status = checkoutService.reserve(userId);
        if (status == ReserveStatus.OK) {
            return ResponseEntity.ok("OK");
        } else {
            return ResponseEntity.badRequest().body(status.name());
        }
    }

    @GetMapping("/place")
    public ResponseEntity<Long> placeOrder(Authentication authentication) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok().body(checkoutService.placeOrder(userId));
    }
}
