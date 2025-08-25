package dev.ecommerce.user.entity;

import dev.ecommerce.product.entity.Product;
import dev.ecommerce.user.constant.UserItemType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_cart")
@Getter
@NoArgsConstructor
public class UserItem {
    @Id
    @GeneratedValue
    private long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Setter
    private int quantity;

    @Setter
    @Enumerated(EnumType.STRING)
    private UserItemType type;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public UserItem(User user, Product product, int quantity, UserItemType type) {
        this.user = user;
        this.product = product;
        this.quantity = quantity;
        this.type = type;
    }
}
