package dev.ecommerce.userInfo.constant;

import lombok.Getter;

@Getter
public enum OrderPlacedWindow {
    DAYS_30(30),
    DAYS_90(90);

    private final Integer days;

    OrderPlacedWindow(int days) { this.days = days; }
}
