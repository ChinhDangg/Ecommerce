package dev.ecommerce.product.metadata;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
public class FilterFieldMetadata {

    private final String field;
    private final String displayName;
    @Setter
    private List<FilterOption> filterOptions;

    public FilterFieldMetadata(String field, String displayName) {
        this.field = field;
        this.displayName = displayName;
    }
}

