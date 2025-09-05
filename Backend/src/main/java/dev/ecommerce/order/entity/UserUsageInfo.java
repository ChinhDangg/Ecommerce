package dev.ecommerce.order.entity;

import dev.ecommerce.user.entity.User;
import dev.ecommerce.user.entity.UserItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Table as helper about retrieving the user info to not tie with the user table directly.
 * Avoiding retrieving user security info.
 * Should be created along as creating the user
 */

@Entity
@Table(name = "user_info")
@Getter
@Setter
@NoArgsConstructor
public class UserUsageInfo {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Instant createdAt;

    private Instant firstOrderAt;

    @OneToMany(mappedBy = "user_info", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final List<UserItem> carts = new ArrayList<>();

    public UserUsageInfo(User user) {
        this.user = user;
    }
}
