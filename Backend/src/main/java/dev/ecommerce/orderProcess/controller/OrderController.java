package dev.ecommerce.orderProcess.controller;

import dev.ecommerce.orderProcess.model.CheckoutDTO;
import dev.ecommerce.orderProcess.service.CheckoutService;
import dev.ecommerce.orderProcess.constant.ReserveStatus;
import dev.ecommerce.user.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/reserve")
    public ResponseEntity<String> reserve(@RequestParam(required = false, defaultValue = "false") boolean extend, Authentication authentication) {
        Long userId = getUserId(authentication);
        ReserveStatus status = checkoutService.reserve(userId, extend);
        if (status == ReserveStatus.OK || status == ReserveStatus.ONGOING) {
            return ResponseEntity.ok(status.name());
        } else {
            return ResponseEntity.badRequest().body(status.name());
        }
    }

    @GetMapping("/checkout")
    public ResponseEntity<CheckoutDTO> getCheckoutInfo(Authentication authentication) {
        Long userId = getUserId(authentication);
        CheckoutDTO checkoutInfo = checkoutService.getUserCheckoutDTO(userId);
        return ResponseEntity.status(HttpStatus.OK).body(checkoutInfo);
    }

    @GetMapping("/place")
    public ResponseEntity<Long> placeOrder(Authentication authentication) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok().body(checkoutService.placeOrder(userId));
    }
}
