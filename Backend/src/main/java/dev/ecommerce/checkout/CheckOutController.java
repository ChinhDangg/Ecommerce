package dev.ecommerce.checkout;

import dev.ecommerce.user.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/check-out")
@RequiredArgsConstructor
public class CheckOutController {

    private final CheckoutService checkoutService;

    @GetMapping("/reserve")
    public ResponseEntity<String> reserve(Authentication authentication) {
        Long userId = ((SecurityUser) authentication.getPrincipal()).user().getId();
        ReserveStatus status = checkoutService.reserve(userId);
        if (status == ReserveStatus.OK) {
            return ResponseEntity.ok("OK");
        } else {
            return ResponseEntity.badRequest().body(status.name());
        }
    }
}
