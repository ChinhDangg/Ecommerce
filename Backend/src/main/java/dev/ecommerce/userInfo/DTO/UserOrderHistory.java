package dev.ecommerce.userInfo.DTO;

import org.springframework.data.domain.Page;

import java.util.List;

public record UserOrderHistory(
        List<TimeFilterOption> timeFilterOptions,
        Page<UserOrderInfo> orders
) {
}
