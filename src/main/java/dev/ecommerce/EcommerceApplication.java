package dev.ecommerce;

import dev.ecommerce.product.DTO.ProductLineDTO;
import dev.ecommerce.product.entity.ContentType;
import dev.ecommerce.product.entity.ProductLine;
import dev.ecommerce.product.entity.ProductLineDescription;
import dev.ecommerce.product.entity.ProductLineMedia;
import dev.ecommerce.product.repository.ProductCategoryRepository;
import dev.ecommerce.product.repository.ProductLineDescriptionRepository;
import dev.ecommerce.product.repository.ProductLineMediaRepository;
import dev.ecommerce.product.repository.ProductLineRepository;
import dev.ecommerce.product.service.ProductService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EcommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceApplication.class, args);
    }

//    @Bean
//    CommandLineRunner commandLineRunner(
//            ProductCategoryRepository productCategoryRepository,
//            ProductLineRepository productLineRepository,
//            ProductLineDescriptionRepository productLineDescriptionRepository,
//            ProductLineMediaRepository productLineMediaRepository,
//            ProductService productService
//            ) {
//        return _ -> {
//            ProductCategory electronics = new ProductCategory("Electronics", null);
//
//            ProductCategory computers = new ProductCategory("Computers", electronics);
//
//            ProductCategory laptops = new ProductCategory("Laptop", computers);
//
//            ProductCategory gamingLaptops = new ProductCategory("GamingLaptops", laptops);
//
//            ProductCategory macs = new ProductCategory("Mac", laptops);
//
//            ProductCategory computerComponent= new ProductCategory("Computer Component", electronics);
//
//            ProductCategory graphicsCard = new ProductCategory("Graphics Card", computerComponent);
//
//            productCategoryRepository.save(electronics);
//            productCategoryRepository.save(computers);
//            productCategoryRepository.save(laptops);
//            productCategoryRepository.save(gamingLaptops);
//            productCategoryRepository.save(macs);
//            productCategoryRepository.save(computerComponent);
//            productCategoryRepository.save(graphicsCard);
//
//            List<ProductCategoryDTO> parentProductCategories = productCategoryRepository.findAllTopParentCategory();
//            System.out.println(parentProductCategories.getFirst().getId());
//            System.out.println(parentProductCategories.getFirst().getName());
//        };
//    }

}
