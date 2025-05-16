package dev.ecommerce.product.repository;

import dev.ecommerce.product.entity.ProductLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductLineRepository extends JpaRepository<ProductLine, Integer> {

//    @EntityGraph(attributePaths = {"descriptions"})
//    @Query("SELECT pl FROM ProductLine pl WHERE pl.id = :id")
//    Optional<ProductLine> findWithDescriptionsById(Integer id);
//
//    @EntityGraph(attributePaths = {"media"})
//    @Query("SELECT pl FROM ProductLine pl WHERE pl.id = :id")
//    Optional<ProductLine> findWithMediaById(Integer id);

}
