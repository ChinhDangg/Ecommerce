package dev.ecommerce.product.constant;

import lombok.Getter;

@Getter
public enum SpecialFilters {
    CATEGORY("pc.name"),
    BRAND("p.brand"),
    PRICE("p.price");

    private final String columnName;

    SpecialFilters(String columnName) {
        this.columnName = columnName;
    }

}
