package dev.ecommerce.user.service;

import dev.ecommerce.exceptionHandler.ResourceNotFoundException;
import dev.ecommerce.product.DTO.ProductMapper;
import dev.ecommerce.product.DTO.ShortProductDTO;
import dev.ecommerce.product.entity.Product;
import dev.ecommerce.product.service.ProductService;
import dev.ecommerce.user.DTO.UserCartDTO;
import dev.ecommerce.user.entity.User;
import dev.ecommerce.user.entity.UserCart;
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

    private UserCart findUserCartByProductId(User user, String username, Long productId, boolean nullable) {
        User findUser = user == null ? findUserByUsername(username) : user;
        Optional<UserCart> userCart = findUser.getCarts().stream()
                .filter(p -> p.getId() == productId)
                .findFirst();
        if (nullable) {
            return userCart.orElse(null);
        } else
            return userCart.orElseThrow(() -> new ResourceNotFoundException("Product not found in cart"));
    }

    private List<UserCart> findUserCart(String username) {
        User findUser = findUserByUsername(username);
        return findUser.getCarts();
    }

    @Transactional(readOnly = true)
    public Integer getCartTotal(String username) {
        List<UserCart> userCart = findUserCart(username);
        if (userCart.isEmpty())
            return 0;
        else
            return userCart.stream().mapToInt(UserCart::getQuantity).sum();
    }

    @Transactional(readOnly = true)
    public List<ShortProductDTO> getCart(String username) {
        List<UserCart> userCart = findUserCart(username);
        if (userCart.isEmpty())
            return new ArrayList<>();

        List<ShortProductDTO> shortProductDTOs = new ArrayList<>();
        for (UserCart cart : userCart) {
            Product product = cart.getProduct();
            product.setQuantity(
                    cart.getQuantity() > product.getQuantity() ? product.getQuantity() : cart.getQuantity()
            );
            ShortProductDTO shortProductDTO = productService.getShortProductInfo(product, false);
            shortProductDTO.setProductOptions(productMapper.toProductOptionDTOList(product.getOptions()));
            shortProductDTOs.add(shortProductDTO);
        }
        return shortProductDTOs;
    }

    @Transactional
    public Integer addProductToCart(String username, UserCartDTO userCartDTO) {
        Product product = productService.findProductById(userCartDTO.getProductId());

        User user = findUserByUsername(username);
        UserCart addedSameCart = findUserCartByProductId(user, username, userCartDTO.getProductId(), true);

        Integer quantity;

        if (addedSameCart == null) {
            quantity = userCartDTO.getQuantity();
            addedSameCart = new UserCart(user, product, quantity);
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
