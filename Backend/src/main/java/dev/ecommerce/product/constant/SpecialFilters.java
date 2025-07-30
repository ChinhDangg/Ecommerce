package dev.ecommerce.product.constant;

import lombok.Getter;

@Getter
public enum SpecialFilters {
    CATEGORY("pc.name", "category.name", true),
    BRAND("p.brand", "brand", false),
    PRICE("p.price", "price", false);

    private final String columnName;
    private final String path;
    private final boolean requiresJoin;

    SpecialFilters(String columnName, String path, boolean requiresJoin) {
        this.columnName = columnName;
        this.path = path;
        this.requiresJoin = requiresJoin;
    }
}
