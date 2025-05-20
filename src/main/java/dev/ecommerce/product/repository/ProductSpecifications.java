package dev.ecommerce.product.repository;

import dev.ecommerce.product.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecifications {

    public static Specification<Product> nameContainsWords(String search) {
        if (search == null) {
            return null;
        }
        String refined = search.replaceAll("[^a-zA-Z0-9 ]", "");
        if (refined.isEmpty()) {
            return null;
        }
        return (root, query, cb) -> {
            String[] words = refined.toLowerCase().split("\\s+");
            Predicate[] predicates = new Predicate[words.length];
            for (int i = 0; i < words.length; i++) {
                predicates[i] = cb.like(cb.lower(root.get("name")), "%" + words[i] + "%");
            }
            return cb.and(predicates);
        };
    }
}
