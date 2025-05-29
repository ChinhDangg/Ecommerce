package dev.ecommerce;

import dev.ecommerce.product.DTO.ProductCategoryDTO;
import dev.ecommerce.product.DTO.ProductLineDTO;
import dev.ecommerce.product.DTO.ShortProductDTO;
import dev.ecommerce.product.entity.*;
import dev.ecommerce.product.repository.*;
import dev.ecommerce.product.service.ProductService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@SpringBootApplication
public class EcommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(
            ProductCategoryRepository productCategoryRepository,
            ProductRepository productRepository,
            ProductService productService,
            ProductLineRepository productLineRepository) {
        return _ -> {
            ProductCategory electronics = new ProductCategory("Electronics", null);
            ProductCategory computers = new ProductCategory("Computers", electronics);
            ProductCategory laptops = new ProductCategory("Laptop", computers);
            ProductCategory gamingLaptops = new ProductCategory("GamingLaptops", laptops);
            ProductCategory macs = new ProductCategory("Mac", laptops);
            ProductCategory computerComponent= new ProductCategory("Computer Component", electronics);
            ProductCategory graphicsCard = new ProductCategory("Graphics Card", computerComponent);

            productCategoryRepository.saveAll(List.of(electronics, computers, laptops, gamingLaptops, macs, computerComponent, graphicsCard));

//            List<ProductCategoryDTO> parentProductCategories = productCategoryRepository.findAllTopParentCategory();
//            System.out.println(parentProductCategories.getFirst().getId());
//            System.out.println(parentProductCategories.getFirst().getName());

//            ProductLine savedProductLine = productLineRepository.save(new ProductLine("Product line name"));
//
//            Product newProduct = new Product(
//                    "Man 2",
//                    "Some name with many different words for testing the search functionality",
//                    "brand",
//                    5,
//                    ConditionType.NEW,
//                    LocalDate.now(),
//                    new BigDecimal("100"),
//                    null,
//                    null,
//                    savedProductLine,
//                    electronics
//            );
//
//            productRepository.save(newProduct);
//
//            Page<ShortProductDTO> shortP = productService.findProductsByName("name many",0);
//            System.out.println(shortP.getTotalElements());
//            System.out.println(shortP.getContent().getFirst().getName());

        };
    }

}
