package dev.ecommerce.userInfo.controller;

import dev.ecommerce.product.DTO.ProductCartDTO;
import dev.ecommerce.user.SecurityUser;
import dev.ecommerce.userInfo.DTO.UserCartDTO;
import dev.ecommerce.userInfo.service.UserInfoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserInfoService userInfoService;

    private Long getUserId(Authentication authentication) {
        if (authentication == null)
            return null;
        return ((SecurityUser) authentication.getPrincipal()).user().getId();
    }

    @GetMapping("/cart/total")
    public ResponseEntity<Integer> getCartTotal(Authentication authentication) {
        Integer cartTotal = userInfoService.getCartTotal(getUserId(authentication));
        return ResponseEntity.status(HttpStatus.OK).body(cartTotal);
    }

    @GetMapping("/cart")
    public ResponseEntity<ProductCartDTO> getCart(Authentication authentication) {
        ProductCartDTO cart = userInfoService.getUserCartInfo(getUserId(authentication));
        return ResponseEntity.status(HttpStatus.OK).body(cart);
    }

    @PostMapping("/cart")
    public ResponseEntity<Integer> addToCart(@Valid @RequestBody UserCartDTO userCartDTO, Authentication authentication) {
        Integer quantity = userInfoService.addProductToCart(getUserId(authentication), userCartDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(quantity);
    }

    @PutMapping("/cart")
    public ResponseEntity<?> updateCartQuantity(@Valid @RequestBody UserCartDTO userCartDTO, Authentication authentication) {
        userInfoService.updateProductQuantityInCart(getUserId(authentication), userCartDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/cart")
    public ResponseEntity<?> deleteFromCart(@RequestParam Long productId, Authentication authentication) {
        userInfoService.removeProductFromCart(getUserId(authentication), productId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cart/to-save")
    public ResponseEntity<?> cartToSaved(@RequestParam Long productId, Authentication authentication) {
        userInfoService.moveProductFromCartToSaved(getUserId(authentication), productId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cart/to-cart")
    public ResponseEntity<?> savedToCart(@RequestParam Long productId, Authentication authentication) {
        userInfoService.moveProductFromSavedToCart(getUserId(authentication), productId);
        return ResponseEntity.ok().build();
    }
}
