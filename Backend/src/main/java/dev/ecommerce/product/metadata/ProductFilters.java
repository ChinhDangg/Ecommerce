package dev.ecommerce.product.metadata;

import java.util.List;

public class ProductFilters {

    private final FilterFieldMetadata brand = new FilterFieldMetadata("brand", "Brand");
    private final FilterFieldMetadata savingsAndStock = new FilterFieldMetadata("savingsAndStock", "Savings & Stock");
    private final FilterFieldMetadata customerRating  = new FilterFieldMetadata("customerRating", "Customer Rating");
    private final FilterFieldMetadata price = new FilterFieldMetadata("price", "Price");

    public List<FilterFieldMetadata> getAllFilterFieldMetadata() {
        return List.of(brand, savingsAndStock, customerRating, price);
    }
}
