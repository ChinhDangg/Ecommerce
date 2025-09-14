package dev.ecommerce.orderProcess.controller;

import dev.ecommerce.orderProcess.service.CheckoutService;
import dev.ecommerce.orderProcess.constant.ReserveStatus;
import dev.ecommerce.user.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

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

    @GetMapping("/check-reserve")
    public ResponseEntity<Map<Long, Map<String, Long>>> checkReserve(Authentication authentication) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(checkoutService.getUserReservations(userId));
    }

    @GetMapping("/place")
    public ResponseEntity<Long> placeOrder(Authentication authentication) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok().body(checkoutService.placeOrder(userId));
    }
}
