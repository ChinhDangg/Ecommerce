package dev.ecommerce.user.controller;

import dev.ecommerce.user.DTO.UserCartDTO;
import dev.ecommerce.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/cart")
    public ResponseEntity<?> addToCart(@RequestBody UserCartDTO userCartDTO, Authentication authentication) {
        userService.addProductToCart(authentication.getName(), userCartDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/cart")
    public ResponseEntity<?> updateCartQuantity(@RequestBody UserCartDTO userCartDTO, Authentication authentication) {
        userService.updateProductQuantityInCart(authentication.getName(), userCartDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/cart")
    public ResponseEntity<?> deleteFromCart(@RequestParam Long productId, Authentication authentication) {
        userService.removeProductFromCart(authentication.getName(), productId);
        return ResponseEntity.ok().build();
    }
}
