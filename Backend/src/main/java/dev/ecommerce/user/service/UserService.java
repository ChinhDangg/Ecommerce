package dev.ecommerce.user.service;

import dev.ecommerce.exceptionHandler.ResourceNotFoundException;
import dev.ecommerce.product.DTO.ProductCartDTO;
import dev.ecommerce.product.DTO.ProductMapper;
import dev.ecommerce.product.DTO.ShortProductDTO;
import dev.ecommerce.product.entity.Product;
import dev.ecommerce.product.service.ProductService;
import dev.ecommerce.user.DTO.UserCartDTO;
import dev.ecommerce.user.constant.UserItemType;
import dev.ecommerce.user.entity.User;
import dev.ecommerce.user.entity.UserItem;
import dev.ecommerce.user.repository.UserCartRepository;
import dev.ecommerce.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserCartRepository userCartRepository;
    private final ProductService productService;
    private final ProductMapper productMapper;

    public UserService(UserRepository userRepository, UserCartRepository userCartRepository, ProductService productService, ProductMapper productMapper) {
        this.userRepository = userRepository;
        this.userCartRepository = userCartRepository;
        this.productService = productService;
        this.productMapper = productMapper;
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(
                () -> new ResourceNotFoundException("User not found")
        );
    }

    private UserItem findUserCartByProductId(User user, String username, Long productId, boolean nullable) {
        User findUser = user == null ? findUserByUsername(username) : user;
        Optional<UserItem> userCart = findUser.getCarts().stream()
                .filter(p -> p.getId() == productId)
                .findFirst();
        if (nullable) {
            return userCart.orElse(null);
        } else
            return userCart.orElseThrow(() -> new ResourceNotFoundException("Product not found in cart"));
    }

    private List<UserItem> findUserCart(String username) {
        User findUser = findUserByUsername(username);
        return findUser.getCarts();
    }

    @Transactional(readOnly = true)
    public Integer getCartTotal(String username) {
        List<UserItem> userItem = findUserCart(username);
        if (userItem.isEmpty())
            return 0;
        else
            return userItem.stream()
                    .filter(ut -> ut.getType() == UserItemType.CART)
                    .mapToInt(UserItem::getQuantity).sum();
    }

    @Transactional(readOnly = true)
    public ProductCartDTO getUserCartInfo(String username) {
        List<UserItem> userItem = findUserCart(username);
        if (userItem.isEmpty())
            return new ProductCartDTO();

        List<ShortProductDTO> shortProductDTOs = new ArrayList<>();
        for (UserItem cart : userItem) {
            Product product = cart.getProduct();
            product.setQuantity(
                    cart.getQuantity() > product.getQuantity() ? product.getQuantity() : cart.getQuantity()
            );
            ShortProductDTO shortProductDTO = productService.getShortProductInfo(product, false);
            shortProductDTO.setProductOptions(productMapper.toProductOptionDTOList(product.getOptions()));
            shortProductDTOs.add(shortProductDTO);
        }
        return productService.getProductCartInfo(shortProductDTOs);
    }

    @Transactional
    public Integer addProductToCart(String username, UserCartDTO userCartDTO) {
        Product product = productService.findProductById(userCartDTO.getProductId());

        User user = findUserByUsername(username);
        UserItem addedSameCart = findUserCartByProductId(user, username, userCartDTO.getProductId(), true);

        Integer quantity;

        if (addedSameCart == null) {
            quantity = userCartDTO.getQuantity();
            addedSameCart = new UserItem(user, product, quantity, UserItemType.CART);
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

        return userCartRepository.save(addedSameCart).getQuantity();
    }

    @Transactional
    public void updateProductQuantityInCart(String username, UserCartDTO userCartDTO) {
        UserItem userItem = findUserCartByProductId(null, username, userCartDTO.getProductId(), false);
        userItem.setQuantity(userItem.getQuantity());
        userCartRepository.save(userItem);
    }

    @Transactional
    public void removeProductFromCart(String username, Long productId) {
        UserItem userItem = findUserCartByProductId(null, username, productId, false);
        userCartRepository.delete(userItem);
    }

    @Transactional
    public void moveProductFromCartToSaved(String username, Long productId) {
        UserItem userItem = findUserCartByProductId(null, username, productId, false);
        if (userItem.getType().equals(UserItemType.SAVED)) {
            throw new IllegalStateException("Moving from saved to saved is not allowed");
        }
        userItem.setType(UserItemType.SAVED);
        userCartRepository.save(userItem);
    }

    @Transactional
    public void moveProductFromSavedToCart(String username, Long productId) {
        UserItem userItem = findUserCartByProductId(null, username, productId, false);
        if (userItem.getType().equals(UserItemType.CART)) {
            throw new IllegalArgumentException("Moving from cart to cart is not allowed");
        }
        userItem.setType(UserItemType.CART);
        userCartRepository.save(userItem);
    }
}
