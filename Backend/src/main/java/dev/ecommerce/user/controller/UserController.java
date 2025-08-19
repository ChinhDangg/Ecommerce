package dev.ecommerce.user.controller;

import dev.ecommerce.product.DTO.ShortProductDTO;
import dev.ecommerce.user.DTO.UserCartDTO;
import dev.ecommerce.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/cart/total")
    public ResponseEntity<Integer> getCartTotal(Authentication authentication) {
        Integer cartTotal = userService.getCartTotal(authentication.getName());
        return ResponseEntity.status(HttpStatus.OK).body(cartTotal);
    }

    @GetMapping("/cart")
    public ResponseEntity<List<ShortProductDTO>> getCart(Authentication authentication) {
        List<ShortProductDTO> cart = userService.getCart(authentication.getName());
        return ResponseEntity.status(HttpStatus.OK).body(cart);
    }

    @PostMapping("/cart")
    public ResponseEntity<Integer> addToCart(@RequestBody UserCartDTO userCartDTO, Authentication authentication) {
        Integer quantity = userService.addProductToCart(authentication.getName(), userCartDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(quantity);
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
