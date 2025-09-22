package dev.ecommerce.userInfo.DTO;

public record UserAddress(
        String street,
        String city,
        String state,
        String zipcode,
        String country
) {
}
