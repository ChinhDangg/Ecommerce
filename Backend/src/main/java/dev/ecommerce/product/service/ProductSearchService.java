package dev.ecommerce.product.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ecommerce.product.DTO.ProductFilterDTO;
import dev.ecommerce.product.DTO.ProductMapper;
import dev.ecommerce.product.DTO.ShortProductDTO;
import dev.ecommerce.product.entity.Product;
import dev.ecommerce.product.entity.ProductSpecification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductSearchService {

    private final EntityManager entityManager;
    private final ProductMapper productMapper;

    public ProductSearchService(EntityManager entityManager, ProductMapper productMapper) {
        this.entityManager = entityManager;
        this.productMapper = productMapper;
    }

    @Transactional(readOnly = true)
    public Page<ShortProductDTO> findProductsByName(String searchString, Map<String, String> selectedSpecs, int page, boolean getFeatures) {
        Pageable pageable = PageRequest.of(page, 10);

        String refined = searchString.replaceAll("[^a-zA-Z0-9 ]", "");
        if (refined.isEmpty())
            return new PageImpl<>(new ArrayList<>(), pageable, 0);

        String[] words = refined.toLowerCase().split("\\s+");
        if (words.length == 0)
            return new PageImpl<>(new ArrayList<>(), pageable, 0);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // --------- Main Query ---------
        CriteriaQuery<Product> query = cb.createQuery(Product.class);
        Root<Product> root = query.from(Product.class);
        Join<Product, ProductSpecification> specJoin = root.join("product_specifications", JoinType.INNER);

        List<Predicate> predicates = buildSearchPredicates(cb, root, specJoin, words, selectedSpecs);

        query.select(root)
                .where(cb.and(predicates.toArray(new Predicate[0])))
                .groupBy(root.get("id"))
                .having(cb.equal(cb.countDistinct(specJoin.get("name")), selectedSpecs.size()));

        TypedQuery<Product> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<Product> resultList = typedQuery.getResultList();
        List<ShortProductDTO> shortProductDTOList = new ArrayList<>();
        for (Product product : resultList) {
            ShortProductDTO current = (getFeatures) ? productMapper.toShortProductWithFeaturesDTO(product)
                    : productMapper.toShortProductWithoutFeaturesDTO(product);
            current.setImageName(product.getMedia().isEmpty() ? null : product.getMedia().getFirst().getContent());
            current.setDiscountedPrice(
                    product.getSaleEndDate() == null ? null : product.getSaleEndDate().isAfter(LocalDate.now()) ? product.getSalePrice() : null
            );
            shortProductDTOList.add(current);
        }

        // --------- Count Query ---------
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Product> countRoot = countQuery.from(Product.class);
        Join<Product, ProductSpecification> countSpecJoin = countRoot.join("product_specifications", JoinType.INNER);

        List<Predicate> countPredicates = buildSearchPredicates(cb, countRoot, countSpecJoin, words, selectedSpecs);

        countQuery.select(countRoot.get("id"))
                .where(cb.and(countPredicates.toArray(new Predicate[0])))
                .groupBy(countRoot.get("id"))
                .having(cb.equal(cb.countDistinct(countSpecJoin.get("name")), selectedSpecs.size()));

        long total = entityManager.createQuery(countQuery).getResultList().size();

        return new PageImpl<>(shortProductDTOList, pageable, total);
    }

    // get predicate by name with product specification - name and option
    private List<Predicate> buildSearchPredicates(
            CriteriaBuilder cb,
            Root<Product> root,
            Join<Product, ProductSpecification> specJoin,
            String[] words,
            Map<String, String> selectedSpecs
    ) {
        List<Predicate> predicates = new ArrayList<>();

        // Keyword match
        for (String word : words) {
            predicates.add(cb.like(cb.lower(root.get("name")), "%" + word.toLowerCase() + "%"));
        }

        // Spec filters
        if (selectedSpecs != null && !selectedSpecs.isEmpty()) {
            List<Predicate> specConditions = new ArrayList<>();
            for (Map.Entry<String, String> entry : selectedSpecs.entrySet()) {
                Predicate nameMatch = cb.equal(cb.lower(specJoin.get("name")), entry.getKey().toLowerCase());
                Predicate optionMatch = cb.equal(cb.lower(specJoin.get("option")), entry.getValue().toLowerCase());
                specConditions.add(cb.and(nameMatch, optionMatch));
            }
            predicates.add(cb.or(specConditions.toArray(new Predicate[0])));
        }

        return predicates;
    }

    private void getProductCoreSpecByName(String[] keywords, Map<String, String> specs) throws JsonProcessingException {
        StringBuilder sql = new StringBuilder("""
           SELECT ps.name,
           jsonb_agg(
               jsonb_build_object(
                 'option', ps.option,
                 'count', COUNT(DISTINCT p.id)
               )
               ORDER BY ps.option
           ) AS option_counts
           FROM product p
           JOIN product_specification ps ON ps.product_id = p.id
           JOIN product_core_specification cps on cps.id = ps.core_specification_id
           WHERE 1=1
        """);

        Map<String, Object> parameters = new HashMap<>();

        for (int i = 0; i < keywords.length; i++) {
            sql.append(" AND LOWER(p.name) LIKE :keyword").append(i);
            parameters.put("keyword" + i, "%" + keywords[i].toLowerCase() + "%");
        }

        int i = 0;
        for (Map.Entry<String, String> entry : specs.entrySet()) {
            sql.append("""
                AND EXISTS (
                    SELECT 1 FROM product_specification ps_sub
                    WHERE ps_sub.product_id = p.id
                      AND LOWER(ps_sub.name) = :specName""").append(i).append("""
                    AND LOWER(ps_sub.option) = :specOption""").append(i).append("""
                )
            """);
            parameters.put("specName" + i, entry.getKey().toLowerCase());
            parameters.put("specOption" + i, entry.getValue().toLowerCase());
            i++;
        }

        sql.append(" GROUP BY ps.name ORDER BY ps.name");

        Query query = entityManager.createNativeQuery(sql.toString());

        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }

        List<Object[]> rows = query.getResultList();
        ObjectMapper objectMapper = new ObjectMapper();
        List<ProductFilterDTO> productFilterList = new ArrayList<>();
        for (Object[] row : rows) {
            String specName = (String) row[0];
            String jsonArray = (String) row[1];

            List<Map<String, Object>> optionCounts =
                    objectMapper.readValue(jsonArray, new TypeReference<>() {});
        }
    }

    private void getProductDetailsAndCount(String[] keywords, Map<String, String> specs) throws JsonProcessingException {
        if (keywords.length == 0)
            return;

        StringBuilder sql = new StringBuilder("""
            WITH filtered_products AS (
               SELECT p.id, p.brand, p.quantity, p.price, pc.name AS category
               FROM product p
               JOIN product_category pc ON pc.id = p.category_id
               JOIN product_specification ps ON ps.product_id = p.id
               JOIN product_core_specification cps on cps.id = ps.core_specification_id
               WHERE 1=1
        """);

        Map<String, Object> parameters = new HashMap<>();

        for (int i = 0; i < keywords.length; i++) {
            sql.append(" AND LOWER(p.name) LIKE :keyword").append(i);
            parameters.put("keyword" + i, "%" + keywords[i].toLowerCase() + "%");
        }

        int i = 0;
        for (Map.Entry<String, String> entry : specs.entrySet()) {
            sql.append("""
                AND EXISTS (
                    SELECT 1 FROM product_specification ps_sub
                    WHERE ps_sub.product_id = p.id
                        AND LOWER(ps_sub.name) = :specName""").append(i).append("""
                    AND LOWER(ps_sub.option) = :specOption""").append(i).append("""
                )
            """);
            parameters.put("specName" + i, entry.getKey().toLowerCase());
            parameters.put("specOption" + i, entry.getValue().toLowerCase());
            i++;
        }

        sql.append("""
            ),
            flattened AS (
                SELECT 'brand' AS field, brand AS value FROM filtered_products
                UNION ALL
                SELECT 'category', category FROM filtered_products
                UNION ALL
                SELECT 'price', price::text FROM filtered_products
                UNION ALL
                SELECT 'quantity', quantity::text FROM filtered_products
            ),
            value_counts AS (
                SELECT field,
                jsonb_agg(
                    jsonb_build_object('value', value, 'count', COUNT(*))
                    ORDER BY value
                ) AS value_counts
                FROM flattened
                GROUP BY field
            ),
            total_products AS (
                SELECT COUNT(*) AS total FROM filtered_products
            )
            SELECT
                (SELECT total FROM total_products) AS product_count,
                jsonb_object_agg(field, value_counts) AS all_counts
            FROM value_counts;
        """);

        Query query = entityManager.createNativeQuery(sql.toString());

        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }

        List<Object[]> rows = query.getResultList();
        ObjectMapper objectMapper = new ObjectMapper();
        if (!rows.isEmpty()) {
            Object[] result = rows.getFirst();
            BigInteger productCount = (BigInteger) result[0];
            String json = result[1].toString(); // or cast to PGobject then getValue()

            Map<String, List<Map<String, Object>>> allCounts =
                    objectMapper.readValue(json, new TypeReference<>() {});
        }
    }

}
