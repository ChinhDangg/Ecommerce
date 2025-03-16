package dev.ecommerce;

import dev.ecommerce.product.entity.ProductCategory;
import dev.ecommerce.product.repository.ProductCategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EcommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(ProductCategoryRepository productCategoryRepository) {
        return _ -> {
            ProductCategory computers = new ProductCategory();
            computers.setName("Computers");

            ProductCategory laptops = new ProductCategory();
            laptops.setName("Laptops");
            laptops.setParentProductCategory(computers); // Hibernate sets parent_id = computers.id

            ProductCategory gamingLaptops = new ProductCategory();
            gamingLaptops.setName("Gaming Laptops");
            gamingLaptops.setParentProductCategory(laptops); // Hibernate sets parent_id = laptops.id

            productCategoryRepository.save(computers);
            productCategoryRepository.save(laptops);
            productCategoryRepository.save(gamingLaptops);

        };
    }

}
