package dev.ecommerce.product.repository;

import dev.ecommerce.product.DTO.ProductCategoryDTO;
import dev.ecommerce.product.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Integer> {

    @Query("SELECT new dev.ecommerce.product.DTO.ProductCategoryDTO(pc.id, pc.name) " +
            "FROM ProductCategory pc WHERE pc.parentProductCategory IS NULL")
    List<ProductCategoryDTO> findAllTopParentCategory();
}
