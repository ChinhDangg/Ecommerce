package dev.ecommerce.userInfo.service;

import dev.ecommerce.exceptionHandler.ResourceNotFoundException;
import dev.ecommerce.userInfo.entity.UserUsageInfo;
import dev.ecommerce.product.DTO.ProductCartDTO;
import dev.ecommerce.product.DTO.ProductMapper;
import dev.ecommerce.product.DTO.ShortProductCartDTO;
import dev.ecommerce.product.entity.Product;
import dev.ecommerce.product.service.ProductService;
import dev.ecommerce.userInfo.DTO.UserCartDTO;
import dev.ecommerce.userInfo.constant.UserItemType;
import dev.ecommerce.userInfo.entity.UserItem;
import dev.ecommerce.userInfo.repository.UserItemRepository;
import dev.ecommerce.userInfo.repository.UserUsageInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserItemService {

    private final UserUsageInfoRepository userInfoRepository;
    private final UserItemRepository userItemRepository;
    private final ProductService productService;
    private final ProductMapper productMapper;

    public UserUsageInfo findUserInfoByUserId(Long userId) {
        return userInfoRepository.findByUserId(userId).orElseThrow(
                () -> new ResourceNotFoundException("User not found with id: " + userId)
        );
    }

    private UserItem findUserCartByProductId(UserUsageInfo userInfo, Long userId, Long productId, boolean nullable) {
        UserUsageInfo findUser = userInfo == null ? findUserInfoByUserId(userId) : userInfo;
        Optional<UserItem> userCart = findUser.getCarts().stream()
                .filter(p -> p.getId() == productId)
                .findFirst();
        if (nullable) {
            return userCart.orElse(null);
        } else
            return userCart.orElseThrow(() -> new ResourceNotFoundException("Product not found in cart"));
    }

    private List<UserItem> findUserCart(Long userId) {
        UserUsageInfo findUser = findUserInfoByUserId(userId);
        return findUser.getCarts();
    }

    @Transactional(readOnly = true)
    public Integer getCartTotal(Long userId) {
        List<UserItem> userItem = findUserCart(userId);
        return userItem.isEmpty() ? 0 : userItem.stream()
                .filter(ut -> ut.getType() == UserItemType.CART)
                .mapToInt(UserItem::getQuantity).sum();
    }

    @Transactional(readOnly = true)
    public ProductCartDTO getUserCartInfo(Long userId) {
        List<UserItem> userItem = findUserCart(userId);
        if (userItem.isEmpty())
            return new ProductCartDTO();

        List<ShortProductCartDTO> shortProductDTOs = productService.getShortProductCartInfo(
                userItem, UserItem::getProduct, UserItem::getQuantity, UserItem::getType
        );
        return productService.getProductCartInfo(shortProductDTOs, false);
    }

    @Transactional
    public Integer addProductToCart(Long userId, UserCartDTO userCartDTO) {
        Product product = productService.findProductById(userCartDTO.getProductId());

        UserUsageInfo userInfo = findUserInfoByUserId(userId);
        UserItem addedSameCart = findUserCartByProductId(userInfo, userId, userCartDTO.getProductId(), true);

        Integer quantity;

        if (addedSameCart == null) {
            quantity = userCartDTO.getQuantity();
            addedSameCart = new UserItem(userInfo, product, quantity, UserItemType.CART);
        } else {
            System.out.println("Same product found in cart");
            quantity = addedSameCart.getQuantity() + userCartDTO.getQuantity();
            addedSameCart.setQuantity(quantity);
        }

        if (!(product.getQuantity() >= quantity)) {
            throw new IllegalArgumentException("Not enough quantity");
        } else if (quantity > 100)
            throw new IllegalArgumentException("Quantity must be less than 100");
        else if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be greater than 0");

        return userItemRepository.save(addedSameCart).getQuantity();
    }

    @Transactional
    public void updateProductQuantityInCart(Long userId, UserCartDTO userCartDTO) {
        UserItem userItem = findUserCartByProductId(null, userId, userCartDTO.getProductId(), false);
        userItem.setQuantity(userItem.getQuantity());
        userItemRepository.save(userItem);
    }

    @Transactional
    public void removeProductFromCart(Long userId, Long productId) {
        UserItem userItem = findUserCartByProductId(null, userId, productId, false);
        userItemRepository.delete(userItem);
    }

    @Transactional
    public void moveProductFromCartToSaved(Long userId, Long productId) {
        UserItem userItem = findUserCartByProductId(null, userId, productId, false);
        if (userItem.getType().equals(UserItemType.SAVED)) {
            throw new IllegalStateException("Moving from saved to saved is not allowed");
        }
        userItem.setType(UserItemType.SAVED);
        userItemRepository.save(userItem);
    }

    @Transactional
    public void moveProductFromSavedToCart(Long userId, Long productId) {
        UserItem userItem = findUserCartByProductId(null, userId, productId, false);
        if (userItem.getType().equals(UserItemType.CART)) {
            throw new IllegalArgumentException("Moving from cart to cart is not allowed");
        }
        userItem.setType(UserItemType.CART);
        userItemRepository.save(userItem);
    }
}
