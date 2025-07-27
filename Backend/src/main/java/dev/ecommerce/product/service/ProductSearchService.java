package dev.ecommerce.product.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ecommerce.product.DTO.ProductMapper;
import dev.ecommerce.product.DTO.ProductSearchResultDTO;
import dev.ecommerce.product.DTO.ShortProductDTO;
import dev.ecommerce.product.constant.SortOption;
import dev.ecommerce.product.constant.SpecialFilters;
import dev.ecommerce.product.entity.Product;
import dev.ecommerce.product.entity.ProductCoreSpecification;
import dev.ecommerce.product.entity.ProductMedia;
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

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class ProductSearchService {

    private final EntityManager entityManager;
    private final ProductMapper productMapper;

    public ProductSearchService(EntityManager entityManager, ProductMapper productMapper) {
        this.entityManager = entityManager;
        this.productMapper = productMapper;
    }

    @Transactional(readOnly = true)
    public ProductSearchResultDTO searchProductByName(String searchString, int page, boolean getFeatures, SortOption sortBy,
                                                      Map<String, List<String>> specialFilters, Map<String, List<String>> selectedSpecs) {
        String refined = searchString.replaceAll("[^a-zA-Z0-9 ]", "");
        if (refined.isEmpty())
            return null;

        String[] words = refined.toLowerCase().split("\\s+");
        if (words.length == 0)
            return null;

        CompletableFuture<ProductCountAndDetails> specialFiltersAndCountFuture =
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return getProductDetailsAndCountByName(words, specialFilters, selectedSpecs);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });

        CompletableFuture<ProductCoreSpecs> specFuture =
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return getProductCoreSpecListByName(words, specialFilters, selectedSpecs);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });

        Map<String, CompletableFuture<?>> futureMap = new HashMap<>();
        futureMap.put("special", specialFiltersAndCountFuture);
        futureMap.put("spec", specFuture);

        boolean hasSpecialFilters = specialFilters != null && !specialFilters.isEmpty();
        boolean hasSelectedSpecs = selectedSpecs != null && !selectedSpecs.isEmpty();

        if (hasSpecialFilters) {
            CompletableFuture<ProductCountAndDetails> fullSpecialFiltersAndCountFuture =
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            return getProductDetailsAndCountByName(words, null, selectedSpecs);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    });
            futureMap.put("fullSpecial", fullSpecialFiltersAndCountFuture);
        }

        if (hasSelectedSpecs) {
            CompletableFuture<ProductCoreSpecs> fullSpecFuture =
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            return getProductCoreSpecListByName(words, specialFilters,null);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    });
            futureMap.put("fullSpec", fullSpecFuture);
        }

        CompletableFuture<Void> allDone = CompletableFuture.allOf(futureMap.entrySet().toArray(new CompletableFuture[0]));
        allDone.join();

        ProductCountAndDetails newProductCountAndDetails = ((ProductCountAndDetails) futureMap.get("special").join());
        if (hasSpecialFilters) {
            Map<String, List<Map<String, Object>>> fullSpecialFilterList = ((ProductCountAndDetails) futureMap.get("fullSpecial").join()).filters;

            updateFullFilters(fullSpecialFilterList, newProductCountAndDetails.filters, specialFilters);
            newProductCountAndDetails.filters.clear();
            newProductCountAndDetails.filters.putAll(fullSpecialFilterList);
        }

        ProductCoreSpecs newSpecList = ((ProductCoreSpecs) futureMap.get("spec").join());
        if (hasSelectedSpecs) {
            Map<String, List<Map<String, Object>>> fullSpecList = ((ProductCoreSpecs) futureMap.get("fullSpec").join()).specs();

            updateFullFilters(fullSpecList, newSpecList.specs, selectedSpecs); // update spec filters
            newSpecList.specs.clear();
            newSpecList.specs.putAll(fullSpecList);
        }

        Page<ShortProductDTO> products = findProductByName(words, specialFilters, selectedSpecs, page, 10, newProductCountAndDetails.count, getFeatures, sortBy);
        return new ProductSearchResultDTO(newProductCountAndDetails.filters, newSpecList.specs, products);
    }

    private void updateFullFilters(Map<String, List<Map<String, Object>>> fullSelectedList,
                      Map<String, List<Map<String, Object>>> newSelectedList,
                      Map<String, List<String>> selectedFilters) {
        for (Map.Entry<String, List<Map<String, Object>>> entry : fullSelectedList.entrySet()) {
            String filter = entry.getKey();

            // Skip the selected filter if it is the only one which is the main branch one
            if (selectedFilters.size() == 1) {
                String selectedFilter = selectedFilters.keySet().iterator().next();
                if (filter.equals(selectedFilter))
                    continue;
            }

            List<Map<String, Object>> fullList = entry.getValue();
            List<Map<String, Object>> newList = newSelectedList.getOrDefault(filter, Collections.emptyList());

            // Build a name → count map from the new list
            Map<String, Integer> nameToCount = new HashMap<>();
            for (Map<String, Object> map : newList) {
                String name = (String) map.get("option");
                Integer count = (Integer) map.get("count");
                nameToCount.put(name, count);
            }

            // Update the full list
            for (Map<String, Object> item : fullList) {
                String name = (String) item.get("option");
                item.put("count", nameToCount.getOrDefault(name, 0));
            }
        }
    }

    @Transactional(readOnly = true)
    protected Page<ShortProductDTO> findProductByName(String[] words, Map<String, List<String>> specialFilers, Map<String, List<String>> selectedSpecs, int page,
                                                      int size, long total, boolean getFeatures, SortOption sortBy) {
        Pageable pageable = PageRequest.of(page, size);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // --------- Main Query ---------
        CriteriaQuery<Product> query = cb.createQuery(Product.class);
        Root<Product> root = query.from(Product.class);
        Join<Product, ProductSpecification> specJoin = root.join("specifications", JoinType.INNER);
        Join<ProductSpecification, ProductCoreSpecification> coreSpecJoin = specJoin.join("productCoreSpecification", JoinType.INNER);

        // create the predicates
        List<Predicate> predicates = new ArrayList<>();
        // Keyword match
        for (String word : words) {
            predicates.add(cb.like(cb.lower(root.get("name")), "%" + word.toLowerCase() + "%"));
        }

        // special filters
        if (specialFilers != null && !specialFilers.isEmpty()) {
            SpecialFilters[] sFilterList = SpecialFilters.values();
            for (SpecialFilters sFilter : sFilterList) {
                List<String> sFilterOptions = specialFilers.remove(sFilter.name().toLowerCase());
                if (sFilterOptions == null || sFilterOptions.isEmpty())
                    continue;
                List<Predicate> sFilterMatches = sFilterOptions.stream()
                        .map(sFOption -> cb.equal(cb.lower(root.get(sFilter.name().toLowerCase())), sFOption.toLowerCase()))
                        .toList();
                predicates.add(cb.or(sFilterMatches.toArray(new Predicate[0])));
            }
        }

        // Spec filters
        if (selectedSpecs != null && !selectedSpecs.isEmpty()) {
            List<Predicate> specGroupPredicates = new ArrayList<>();

            for (Map.Entry<String, List<String>> entry : selectedSpecs.entrySet()) {
                String name = entry.getKey().toLowerCase();
                List<String> options = entry.getValue();

                Predicate nameMatch = cb.equal(cb.lower(specJoin.get("name")), name);

                // Each option becomes a separate predicate
                List<Predicate> optionMatches = options.stream()
                        .map(opt -> cb.equal(cb.lower(specJoin.get("option")), opt.toLowerCase()))
                        .toList();

                // Combine into: (name = 'ram' AND (option = '16gb' OR option = '48gb'))
                Predicate fullMatch = cb.and(nameMatch, cb.or(optionMatches.toArray(new Predicate[0])));
                specGroupPredicates.add(fullMatch);
            }

            // Add to the global predicate list: all spec groups must be satisfied
            predicates.add(cb.or(specGroupPredicates.toArray(new Predicate[0])));
        }

        // create the query
        query.select(root)
                .where(cb.and(predicates.toArray(new Predicate[0])))
                .groupBy(root.get("id"));

        if (selectedSpecs != null && !selectedSpecs.isEmpty()) { // handle when no selected specs
            query.having(cb.equal(cb.countDistinct(specJoin.get("name")), selectedSpecs.size()));
        }

        switch (sortBy) {
            case PRICE_ASC -> query.orderBy(cb.asc(root.get("price")));
            case PRICE_DESC -> query.orderBy(cb.desc(root.get("price")));
            case NEWEST -> query.orderBy(cb.desc(root.get("addedDate")));
            case TOP_RATED -> query.orderBy(cb.desc(root.get("totalRatings")));
            case MOST_RATED -> query.orderBy(cb.desc(root.get("totalReviews")));
            case BEST_SELLING -> query.orderBy(cb.desc(root.get("totalSold")));
        }

        TypedQuery<Product> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<Product> resultList = typedQuery.getResultList();
        List<ShortProductDTO> shortProductDTOList = new ArrayList<>();
        for (Product product : resultList) {
            ShortProductDTO current = (getFeatures) ? productMapper.toShortProductWithFeaturesDTO(product)
                    : productMapper.toShortProductWithoutFeaturesDTO(product);
            current.setImageName(product.getMedia().stream().findFirst().map(ProductMedia::getContent).orElse(null));
            current.setDiscountedPrice(
                    product.getSaleEndDate() == null ? null : product.getSaleEndDate().isAfter(LocalDate.now()) ? product.getSalePrice() : null
            );
            if (product.getSaleEndDate() != null) {
                long daysDifference = ChronoUnit.DAYS.between(product.getSaleEndDate(), LocalDate.now());
                current.setNewRelease(daysDifference >= 0 && daysDifference < 8);
            } else {
                current.setNewRelease(false);
            }
            shortProductDTOList.add(current);
        }

        return new PageImpl<>(shortProductDTOList, pageable, total);
    }

    // Map<String, List<Map<String, Object>>> specs
    private ProductCoreSpecs getProductCoreSpecListByName(String[] keywords, Map<String, List<String>> selectedFilters, Map<String, List<String>> specs) throws JsonProcessingException {
        if (keywords.length == 0)
            return new ProductCoreSpecs(null);

        StringBuilder sql = new StringBuilder("""
            SELECT
            ps_group.name,
            jsonb_agg(
                jsonb_build_object(
                    'option', ps_group.option,
                    'count', ps_group.count
                ) ORDER BY ps_group.option
            ) AS option_counts
                FROM (
                SELECT ps.name, ps.option, COUNT(DISTINCT p.id) AS count
                FROM product p
                JOIN product_specification ps ON ps.product_id = p.id
                JOIN product_core_specification cps ON cps.id = ps.core_specification_id
        """);

        // special filters
        if (selectedFilters != null && !selectedFilters.isEmpty()) {
            if (selectedFilters.containsKey(SpecialFilters.CATEGORY.name().toLowerCase())) {
                sql.append(" JOIN product_category pc ON pc.id = p.category_id ");
            }
        }

        sql.append(" WHERE 1=1");

        Map<String, Object> parameters = new HashMap<>();

        if (selectedFilters != null && !selectedFilters.isEmpty()) {
            int i = 0;
            for (SpecialFilters filter : SpecialFilters.values()) {
                String key = filter.name().toLowerCase();
                List<String> options = selectedFilters.remove(key);
                if (options == null || options.isEmpty())
                    continue;

                sql.append(" AND (");
                for (int j = 0; j < options.size(); j++) {
                    if (j > 0) sql.append(" OR ");
                    sql.append("LOWER(")
                            .append(filter.getColumnName())
                            .append(") = :filter")
                            .append(i).append("_").append(j);
                    parameters.put("filter" + i + "_" + j, options.get(j).toLowerCase());
                }
                sql.append(")");
                i++;
            }
        }

        for (int i = 0; i < keywords.length; i++) {
            sql.append(" AND LOWER(p.name) LIKE :keyword").append(i);
            parameters.put("keyword" + i, "%" + keywords[i].toLowerCase() + "%");
        }

        int i = 0;
        for (Map.Entry<String, List<String>> entry : specs.entrySet()) {
            String specName = entry.getKey().toLowerCase();
            List<String> options = entry.getValue();
            sql.append(" AND EXISTS (")
                    .append(" SELECT 1 FROM product_specification ps_sub")
                    .append(" WHERE ps_sub.product_id = p.id")
                    .append(" AND LOWER(ps_sub.name) = :specName").append(i)
                    .append(" AND (");

            for (int j = 0; j < options.size(); j++) {
                if (j > 0) sql.append(" OR ");
                sql.append("LOWER(ps_sub.option) = :specOption")
                        .append(i).append("_").append(j);
                parameters.put("specOption" + i + "_" + j, options.get(j).toLowerCase());
            }

            sql.append(")) "); // close EXISTS

            parameters.put("specName" + i, specName);
            i++;
        }

        sql.append(""" 
                GROUP BY ps.name, ps.option
            ) ps_group
            GROUP BY ps_group.name
            ORDER BY ps_group.name
        """);

        Query query = entityManager.createNativeQuery(sql.toString());

        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }

        List<Object[]> rows = query.getResultList();
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, List<Map<String, Object>>> productFilterList = new HashMap<>();
        for (Object[] row : rows) {
            String specName = (String) row[0];
            String jsonArray = (String) row[1];

            List<Map<String, Object>> optionCounts =
                    objectMapper.readValue(jsonArray, new TypeReference<>() {});
            productFilterList.put(specName, optionCounts);
        }
        return new ProductCoreSpecs(productFilterList);
    }

    private record ProductCoreSpecs(Map<String, List<Map<String, Object>>> specs) {}

    private ProductCountAndDetails getProductDetailsAndCountByName(String[] keywords, Map<String, List<String>> selectedFilters,
                                                                   Map<String, List<String>> specs) throws JsonProcessingException {
        if (keywords.length == 0)
            return null;

        StringBuilder sql = new StringBuilder("""
            WITH matched_ids AS (
                SELECT DISTINCT p.id
                FROM product p
                JOIN product_specification ps ON ps.product_id = p.id
                JOIN product_core_specification cps ON cps.id = ps.core_specification_id
        """);

        // special filters
        if (selectedFilters != null && !selectedFilters.isEmpty()) {
            if (selectedFilters.containsKey(SpecialFilters.CATEGORY.name().toLowerCase())) {
                sql.append(" JOIN product_category pc ON pc.id = p.category_id ");
            }
        }

        sql.append(" WHERE 1=1");

        Map<String, Object> parameters = new HashMap<>();

        if (selectedFilters != null && !selectedFilters.isEmpty()) {
            int i = 0;
            for (SpecialFilters filter : SpecialFilters.values()) {
                String key = filter.name().toLowerCase();
                List<String> options = selectedFilters.remove(key);
                if (options == null || options.isEmpty())
                    continue;

                sql.append(" AND (");
                for (int j = 0; j < options.size(); j++) {
                    if (j > 0) sql.append(" OR ");
                    sql.append("LOWER(")
                            .append(filter.getColumnName())
                            .append(") = :filter")
                            .append(i).append("_").append(j);
                    parameters.put("filter" + i + "_" + j, options.get(j).toLowerCase());
                }
                sql.append(")");
                i++;
            }
        }

        for (int i = 0; i < keywords.length; i++) {
            sql.append(" AND LOWER(p.name) LIKE :keyword").append(i);
            parameters.put("keyword" + i, "%" + keywords[i].toLowerCase() + "%");
        }

        int i = 0;
        for (Map.Entry<String, List<String>> entry : specs.entrySet()) {
            String specName = entry.getKey().toLowerCase();
            List<String> options = entry.getValue();
            sql.append(" AND EXISTS (")
                    .append(" SELECT 1 FROM product_specification ps_sub")
                    .append(" WHERE ps_sub.product_id = p.id")
                    .append(" AND LOWER(ps_sub.name) = :specName").append(i)
                    .append(" AND (");

            for (int j = 0; j < options.size(); j++) {
                if (j > 0) sql.append(" OR ");
                sql.append("LOWER(ps_sub.option) = :specOption")
                        .append(i).append("_").append(j);
                parameters.put("specOption" + i + "_" + j, options.get(j).toLowerCase());
            }

            sql.append(")) "); // close EXISTS

            parameters.put("specName" + i, specName);
            i++;
        }

        sql.append("""
            ),
            filtered_products AS (
                SELECT p.id, p.brand, p.price, pc.name AS category
                FROM product p
                JOIN product_category pc ON pc.id = p.category_id
                WHERE p.id IN (SELECT id FROM matched_ids)
            ),
            flattened AS (
                SELECT 'brand' AS field, brand AS option FROM filtered_products
                UNION ALL
                SELECT 'category', category FROM filtered_products
                UNION ALL
                SELECT 'price', price::text FROM filtered_products
            ),
            option_grouped AS (
                SELECT field, option, COUNT(*) AS count
                FROM flattened
                GROUP BY field, option
            ),
            option_counts AS (
                SELECT field,
                    jsonb_agg(
                        jsonb_build_object('option', option, 'count', count)
                        ORDER BY option
                    ) AS option_counts
                FROM option_grouped
                GROUP BY field
            ),
            total_products AS (
                SELECT COUNT(*) AS total FROM filtered_products
            )
            SELECT
                (SELECT total FROM total_products) AS product_count,
                field,
                option_counts
            FROM option_counts;
        """);

        /*
        get id, brand, quantity, price, category.name with filters
        flatten the result into 2 columns: field | option
                                           brand | Lenovo
                                           brand | ASUS
                                           category | gaming laptop
                                           category | notebook
        group by field and option, and get field, option, and count of them
                                            field | option | count
                                            brand | ASUS  | 4
        group by field and get field and option_counts
                                            field | option_grouped
                                            brand | [{'option':'ASUS', 'count':4},{'option':'Lenovo','count':5}]
         */

        Query query = entityManager.createNativeQuery(sql.toString());

        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }

        List<Object[]> rows = query.getResultList();
        ObjectMapper objectMapper = new ObjectMapper();
        /*
     *      {
         *      "product_count": 18,
         *      "field": "brand",
         *      "option_counts" : [{"option": "Lenovo", "count": 10}, {"option": "Dell", "count": 8}],
         *  },
         *  {
         *      "product_count": 10,
         *      "field": "category",
         *      "option_counts": [{"option": "gaming laptop", "count": 10}, {"option": "notebook", "count": 8}],
         *  }
         */
        if (!rows.isEmpty()) {
            int productCount = ((Number) rows.getFirst()[0]).intValue();

            Map<String, List<Map<String, Object>>> filters = new HashMap<>();
            for (Object[] row : rows) {
                String field = (String) row[1];
                String json = (String) row[2]; // assuming option_counts is returned as JSON string
                List<Map<String, Object>> optionCounts = objectMapper.readValue(json, new TypeReference<>() {});
                filters.put(field, optionCounts);
            }
            return new ProductCountAndDetails(productCount, filters);
        }
        return new ProductCountAndDetails(0, null);
    }

    private record ProductCountAndDetails(
            int count,
            Map<String, List<Map<String, Object>>> filters
    ) {}
}
