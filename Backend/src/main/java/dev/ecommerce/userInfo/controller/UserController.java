package dev.ecommerce.userInfo.controller;

import dev.ecommerce.product.DTO.ProductCartDTO;
import dev.ecommerce.user.SecurityUser;
import dev.ecommerce.userInfo.DTO.*;
import dev.ecommerce.userInfo.service.UserInfoService;
import dev.ecommerce.userInfo.service.UserItemService;
import dev.ecommerce.userInfo.service.UserOrderService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserItemService userItemService;
    private final UserOrderService userOrderService;
    private final UserInfoService userInfoService;

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

    @DeleteMapping("/cart/{productId}")
    public ResponseEntity<?> deleteFromCart(@PathVariable Long productId, Authentication authentication) {
        userItemService.removeProductFromCart(getUserId(authentication), productId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cart/to-save/{productId}")
    public ResponseEntity<?> cartToSaved(@PathVariable Long productId, Authentication authentication) {
        userItemService.moveProductFromCartToSaved(getUserId(authentication), productId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cart/to-cart/{productId}")
    public ResponseEntity<?> savedToCart(@PathVariable Long productId, Authentication authentication) {
        userItemService.moveProductFromSavedToCart(getUserId(authentication), productId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/order/history")
    public ResponseEntity<UserOrderHistory> getUserOrderHistory(@RequestParam(required = false) String orderPlacedWindow,
                                                                @RequestParam(required = false) Integer page,
                                                                Authentication authentication) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok().body(userOrderService.getUserOrderHistory(userId, orderPlacedWindow, page, 10));
    }

    @GetMapping("/info")
    public ResponseEntity<UserBasicInfo> getUserInfo(Authentication authentication) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok().body(userInfoService.getUserBasicInfo(userId));
    }

    @PutMapping("/info/name")
    public ResponseEntity<String> updateUserName(@RequestBody UserBasicInfo userBasicInfo, Authentication authentication) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok().body(userInfoService.updateFirstNameAndLastName(userId, userBasicInfo));
    }

    @PutMapping("/info/email")
    public ResponseEntity<String> updateUserEmail(@RequestBody UserBasicInfo userBasicInfo,
                                                  Authentication authentication,
                                                  HttpServletResponse response) {
        Long userId = getUserId(authentication);
        Pair<ResponseCookie, String> updated = userInfoService.updateEmail(userId, userBasicInfo);
        response.addHeader(HttpHeaders.SET_COOKIE, updated.getFirst().toString());
        return ResponseEntity.ok().body(updated.getSecond());
    }

    @PutMapping("/info/password")
    public ResponseEntity<String> updateUserPassword(@RequestBody UserBasicInfo userBasicInfo, Authentication authentication) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok().body(userInfoService.updatePassword(userId, userBasicInfo));
    }

    @GetMapping("/info/address")
    public ResponseEntity<UserAddress> getAddress(Authentication authentication) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok().body(userInfoService.getAddress(userId));
    }

    @PutMapping("/info/address")
    public ResponseEntity<UserAddress> updateAddress(@RequestBody UserAddress userAddress, Authentication authentication) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok().body(userInfoService.updateAddress(userId, userAddress));
    }

    @GetMapping("/review/{productId}")
    public ResponseEntity<UserProductReviewInfo> getUserProductReviewInfo(@PathVariable Long productId,
                                                                          Authentication authentication) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok().body(userOrderService.getUserProductReviewInfo(userId, productId));
    }

    @PostMapping(value="/review", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> addUserProductReview(@RequestPart("review") @Valid UserProductReviewInfo userProductReviewInfo,
                                                                      @RequestPart(value="image", required=false) MultipartFile image,
                                                                      Authentication authentication) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok().body(userOrderService.addUserProductReviewInfo(userId, userProductReviewInfo, image));
    }

}
