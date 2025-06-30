package dev.ecommerce.product.DTO;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class ProductFilterDTO {

    @Getter
    private final List<String> filterFields = new ArrayList<>();

    public ProductFilterDTO() {
        filterFields.add("Brand");
        filterFields.add("Savings & Stock");
        filterFields.add("Customer Rating");
        filterFields.add("Price");
    }

    public void addFilterField(String filterField) {
        filterFields.add(filterField);
    }
}
