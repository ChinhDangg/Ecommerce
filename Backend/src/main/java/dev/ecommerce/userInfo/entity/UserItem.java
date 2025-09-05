package dev.ecommerce.userInfo.entity;

import dev.ecommerce.order.entity.UserUsageInfo;
import dev.ecommerce.product.entity.Product;
import dev.ecommerce.userInfo.constant.UserItemType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_item")
@Getter
@NoArgsConstructor
public class UserItem {
    @Id
    @GeneratedValue
    private long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Setter
    private int quantity;

    @Setter
    @Enumerated(EnumType.STRING)
    private UserItemType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_info_id", nullable = false)
    private UserUsageInfo userUsageInfo;

    public UserItem(UserUsageInfo userUsageInfo, Product product, int quantity, UserItemType type) {
        this.userUsageInfo = userUsageInfo;
        this.product = product;
        this.quantity = quantity;
        this.type = type;
    }
}
