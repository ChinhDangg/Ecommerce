package dev.ecommerce.userInfo.controller;

import dev.ecommerce.product.DTO.ProductCartDTO;
import dev.ecommerce.user.SecurityUser;
import dev.ecommerce.userInfo.DTO.UserCartDTO;
import dev.ecommerce.userInfo.DTO.UserOrderHistory;
import dev.ecommerce.userInfo.constant.OrderPlacedWindow;
import dev.ecommerce.userInfo.service.UserItemService;
import dev.ecommerce.userInfo.service.UserOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserItemService userItemService;
    private final UserOrderService userOrderService;

    private Long getUserId(Authentication authentication) {
        if (authentication == null)
            return null;
        return ((SecurityUser) authentication.getPrincipal()).user().getId();
    }

    @GetMapping("/cart/total")
    public ResponseEntity<Integer> getCartTotal(Authentication authentication) {
        Integer cartTotal = userItemService.getCartTotal(getUserId(authentication));
        return ResponseEntity.status(HttpStatus.OK).body(cartTotal);
    }

    @GetMapping("/cart")
    public ResponseEntity<ProductCartDTO> getCart(Authentication authentication) {
        ProductCartDTO cart = userItemService.getUserCartInfo(
                getUserId(authentication), false, true, false);
        return ResponseEntity.status(HttpStatus.OK).body(cart);
    }

    @PostMapping("/cart")
    public ResponseEntity<Integer> addToCart(@Valid @RequestBody UserCartDTO userCartDTO, Authentication authentication) {
        Integer quantity = userItemService.addProductToCart(getUserId(authentication), userCartDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(quantity);
    }

    @PutMapping("/cart")
    public ResponseEntity<?> updateCartQuantity(@Valid @RequestBody UserCartDTO userCartDTO, Authentication authentication) {
        userItemService.updateProductQuantityInCart(getUserId(authentication), userCartDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/cart")
    public ResponseEntity<?> deleteFromCart(@RequestParam Long productId, Authentication authentication) {
        userItemService.removeProductFromCart(getUserId(authentication), productId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cart/to-save")
    public ResponseEntity<?> cartToSaved(@RequestParam Long productId, Authentication authentication) {
        userItemService.moveProductFromCartToSaved(getUserId(authentication), productId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cart/to-cart")
    public ResponseEntity<?> savedToCart(@RequestParam Long productId, Authentication authentication) {
        userItemService.moveProductFromSavedToCart(getUserId(authentication), productId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/order/history")
    public ResponseEntity<UserOrderHistory> getUserOrderHistory(@RequestParam(required = false) OrderPlacedWindow orderPlacedWindow,
                                                                @RequestParam(required = false) int page, Authentication authentication) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok().body(userOrderService.getUserOrderHistory(userId, start, end, page, 10));
    }
}
