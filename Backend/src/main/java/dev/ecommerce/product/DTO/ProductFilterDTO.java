package dev.ecommerce.product.DTO;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProductFilterDTO {

    @Getter
    private final List<String> filterFields = new ArrayList<>();

    private String specName;
    private List<Map<String, Object>> optionCounts;

    public ProductFilterDTO() {
        filterFields.add("Category");
        filterFields.add("Brand");
        filterFields.add("Savings & Stock");
        filterFields.add("Customer Rating");
        filterFields.add("Price");
    }

    public void addFilterField(String filterField) {
        filterFields.add(filterField);
    }
}
