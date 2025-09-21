package dev.ecommerce.userInfo.entity;

import dev.ecommerce.orderProcess.entity.Order;
import dev.ecommerce.user.entity.User;
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
@NoArgsConstructor
public class UserUsageInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "userInfo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final List<UserItem> carts = new ArrayList<>();

    @Getter(AccessLevel.NONE) // should not be retrieving all by getter - must be limit
    @OneToMany(mappedBy = "userInfo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final List<Order> orders = new ArrayList<>();

    private Instant createdAt;

    @Setter
    private String displayName;

    @Setter
    private Instant firstOrderAt;

    @Setter
    private String street = "";
    @Setter
    private String city = "";
    @Setter
    private String state = "";
    @Setter
    private String zipCode = "";
    @Setter
    private String country = "";

    public UserUsageInfo(User user, Instant createdAt) {
        this.createdAt = createdAt;
        this.user = user;
    }

    public void setAddress(String street, String city, String state, String zipCode, String country) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.country = country;
    }

    // temporary for now
    public String getUserAddress() {
        if (!hasAddress())
            return null;
        return street + ", " + city + ", " + state + ", " + zipCode + ", " + country;
    }

    public boolean hasAddress() {
        return !street.isEmpty() && !city.isEmpty() && !state.isEmpty() && !zipCode.isEmpty() && !country.isEmpty();
    }
}
