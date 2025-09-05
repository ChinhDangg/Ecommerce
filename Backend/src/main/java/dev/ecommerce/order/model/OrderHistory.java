package dev.ecommerce.order.model;

import org.springframework.data.domain.Page;

import java.util.List;

public record OrderHistory(
        List<TimeFilterOption> timeFilterOptions,
        Page<OrderInfo> orders
) {
}
