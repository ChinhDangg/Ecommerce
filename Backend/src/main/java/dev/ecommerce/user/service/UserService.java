package dev.ecommerce.user.service;

import dev.ecommerce.product.entity.Product;
import dev.ecommerce.product.service.ProductService;
import dev.ecommerce.user.DTO.UserCartDTO;
import dev.ecommerce.user.entity.User;
import dev.ecommerce.user.entity.UserCart;
import dev.ecommerce.user.repository.UserCartRepository;
import dev.ecommerce.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserCartRepository userCartRepository;
    public ProductService productService;

    public UserService(UserRepository userRepository, UserCartRepository userCartRepository) {
        this.userRepository = userRepository;
        this.userCartRepository = userCartRepository;
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(
                () -> new RuntimeException("User not found")
        );
    }

    private UserCart findUserCartByProductId(User user, String username, Long productId, boolean nullable) {
        User findUser = user == null ? findUserByUsername(username) : user;
        Optional<UserCart> userCart = findUser.getCarts().stream()
                .filter(p -> p.getId() == productId)
                .findFirst();
        if (nullable) {
            return userCart.orElse(null);
        } else
            return userCart.orElseThrow(() -> new RuntimeException("Product not found in cart"));
    }

    @Transactional
    public void addProductToCart(String username, UserCartDTO userCartDTO) {
        Product product = productService.findProductById(userCartDTO.getProductId());

        User user = findUserByUsername(username);
        UserCart addedSameCart = findUserCartByProductId(user, username, userCartDTO.getProductId(), true);

        Integer quantity;

        if (addedSameCart == null) {
            quantity = userCartDTO.getQuantity();
            addedSameCart = new UserCart(user, product, quantity);
        } else {
            System.out.println("Same product found in cart");
            quantity = addedSameCart.getProduct().getQuantity() + userCartDTO.getQuantity();
            addedSameCart.setQuantity(quantity);
        }

        if (!(product.getQuantity() >= quantity)) {
            throw new RuntimeException("Not enough quantity");
        } else if (quantity > 100)
            throw new RuntimeException("Quantity must be less than 100");

        userCartRepository.save(addedSameCart);
    }

    @Transactional
    public void updateProductQuantityInCart(String username, UserCartDTO userCartDTO) {
        UserCart userCart = findUserCartByProductId(null, username, userCartDTO.getProductId(), false);
        userCart.setQuantity(userCart.getQuantity());
        userCartRepository.save(userCart);
    }

    @Transactional
    public void removeProductFromCart(String username, Long productId) {
        UserCart userCart = findUserCartByProductId(null, username, productId, false);
        userCartRepository.delete(userCart);
    }
}
