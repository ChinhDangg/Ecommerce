package dev.ecommerce;

import dev.ecommerce.product.constant.ConditionType;
import dev.ecommerce.product.constant.ContentType;
import dev.ecommerce.product.entity.*;
import dev.ecommerce.product.repository.*;
import dev.ecommerce.product.service.ProductService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

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
            ProductLineRepository productLineRepository,
            ProductLineMediaRepository productLineMediaRepository,
            ProductLineDescriptionRepository productLineDescriptionRepository,
            ProductMediaRepository productMediaRepository,
            ProductDescriptionRepository productDescriptionRepository,
            ProductFeatureRepository productFeatureRepository,
            ProductOptionRepository productOptionRepository,
            ProductSpecificationRepository productSpecificationRepository,
            ProductCoreSpecificationRepository productCoreSpecificationRepository) {
        return _ -> {
            ProductCategory electronics = new ProductCategory("Electronics", null);
            ProductCategory computers = new ProductCategory("Computers", electronics);
            ProductCategory display = new ProductCategory("Display", electronics);
            ProductCategory laptops = new ProductCategory("Laptops", computers);
            ProductCategory gamingLaptops = new ProductCategory("GamingLaptops", laptops);
            ProductCategory macs = new ProductCategory("Mac", laptops);
            ProductCategory computerComponent= new ProductCategory("Computer Component", electronics);
            ProductCategory graphicsCard = new ProductCategory("Graphics Card", computerComponent);

            productCategoryRepository.saveAll(List.of(electronics, computers, laptops, gamingLaptops, macs, computerComponent, graphicsCard));

            ProductCoreSpecification displaySizeSpec = new ProductCoreSpecification("Display Size", laptops);
            ProductCoreSpecification monitorResolutionSpec = new ProductCoreSpecification("Monitor Resolution", laptops);
            ProductCoreSpecification processorSpec = new ProductCoreSpecification("Processor", computers);
            ProductCoreSpecification ramSpec = new ProductCoreSpecification("RAM", computers);
            ProductCoreSpecification storageSpec = new ProductCoreSpecification("Storage Size", computers);
            productCoreSpecificationRepository.saveAll(List.of(displaySizeSpec, monitorResolutionSpec, processorSpec, ramSpec, storageSpec));

            ProductLine savedProductLine = productLineRepository.save(new ProductLine("Product line name"));
            ProductLineMedia productLineMedia = new ProductLineMedia(
                    savedProductLine,
                    ContentType.IMAGE,
                    "/images/水淼Aqua cosplay Tsukatsuki Rio - Blue Archive (5).jpg",
                    0
            );
            productLineMediaRepository.save(productLineMedia);
            ProductLineDescription productLineDescription = new ProductLineDescription(
                    savedProductLine,
                    ContentType.IMAGE,
                    "/images/水淼Aqua cosplay Tsukatsuki Rio - Blue Archive (5).jpg",
                    0
            );
            ProductLineDescription productLineDescription2 = new ProductLineDescription(
                savedProductLine,
                ContentType.TEXT,
                "Product Line Description",
                1
            );
            productLineDescriptionRepository.saveAll(List.of(productLineDescription, productLineDescription2));

            Product product1 = new Product(
                    "Man part 1",
                    "Product Name 1",
                    "Brand 1",
                    5,
                    ConditionType.NEW,
                    LocalDate.now(),
                    new BigDecimal("100"),
                    null,
                    null,
                    savedProductLine,
                    electronics
            );
            productRepository.save(product1);
            ProductOption product1Option1 = new ProductOption(
                    product1,
                    savedProductLine,
                    "Option 1",
                    "Option 1 value 1"
            );
            ProductOption product1Option2 = new ProductOption(
                    product1,
                    savedProductLine,
                    "Option 2",
                    "Option 2 value 1"
            );
            productOptionRepository.saveAll(List.of(product1Option1, product1Option2));
            ProductSpecification product1Spec1 = new ProductSpecification(
                    product1,
                    processorSpec,
                    "CPU",
                    "Core i9"
            );
            ProductSpecification product1Spec2 = new ProductSpecification(
                    product1,
                    ramSpec,
                    "RAM",
                    "16GB"
            );
            productSpecificationRepository.saveAll(List.of(product1Spec1, product1Spec2));
            ProductFeature product1Feature1 = new ProductFeature(
                    product1,
                    "Product 1 feature 1"
            );
            ProductFeature product1Feature2 = new ProductFeature(
                    product1,
                    "Product 1 feature 2"
            );
            ProductFeature product1Feature3 = new ProductFeature(
                    product1,
                    "Product 1 feature 3"
            );
            productFeatureRepository.saveAll(List.of(product1Feature1, product1Feature2, product1Feature3));
            ProductMedia newProductMedia = new ProductMedia(
                    product1,
                    ContentType.IMAGE,
                    "/images/水淼Aqua cosplay Tsukatsuki Rio - Blue Archive (5).jpg",
                    0
            );
            productMediaRepository.save(newProductMedia);
            ProductDescription product1Description1 = new ProductDescription(
                    product1,
                    ContentType.IMAGE,
                    "/images/水淼Aqua cosplay Tsukatsuki Rio - Blue Archive (5).jpg",
                    0
            );
            ProductDescription product1Description2 = new ProductDescription(
                    product1,
                    ContentType.TEXT,
                    "Product 1 Description",
                    1
            );
            productDescriptionRepository.saveAll(List.of(product1Description1, product1Description2));


            Product product2 = new Product(
                    "Man part 2",
                    "Product Name 2",
                    "Brand 1",
                    5,
                    ConditionType.NEW,
                    LocalDate.now(),
                    new BigDecimal("100"),
                    null,
                    null,
                    savedProductLine,
                    electronics
            );
            productRepository.save(product2);
            ProductOption product2Option1 = new ProductOption(
                    product2,
                    savedProductLine,
                    "Option 1",
                    "Option 1 value 2"
            );
            ProductOption product2Option2 = new ProductOption(
                    product2,
                    savedProductLine,
                    "Option 2",
                    "Option 2 value 2"
            );
            productOptionRepository.saveAll(List.of(product2Option1, product2Option2));
            ProductSpecification product2Spec1 = new ProductSpecification(
                    product2,
                    processorSpec,
                    "CPU",
                    "Core i7"
            );
            ProductSpecification product2Spec2 = new ProductSpecification(
                    product2,
                    ramSpec,
                    "RAM",
                    "32GB"
            );
            productSpecificationRepository.saveAll(List.of(product2Spec1, product2Spec2));
            ProductFeature product2Feature1 = new ProductFeature(
                    product2,
                    "Product 2 feature 1"
            );
            ProductFeature product2Feature2 = new ProductFeature(
                    product2,
                    "Product 2 feature 2"
            );
            ProductFeature product2Feature3 = new ProductFeature(
                    product2,
                    "Product 2 feature 3"
            );
            productFeatureRepository.saveAll(List.of(product2Feature1, product2Feature2, product2Feature3));
            ProductMedia newProductMedia2 = new ProductMedia(
                    product2,
                    ContentType.IMAGE,
                    "/images/水淼Aqua cosplay Tsukatsuki Rio - Blue Archive (5).jpg",
                    0
            );
            productMediaRepository.save(newProductMedia2);
            ProductDescription product2Description1 = new ProductDescription(
                    product2,
                    ContentType.IMAGE,
                    "/images/水淼Aqua cosplay Tsukatsuki Rio - Blue Archive (5).jpg",
                    0
            );
            ProductDescription product2Description2 = new ProductDescription(
                    product2,
                    ContentType.TEXT,
                    "Product 2 Description",
                    1
            );
            productDescriptionRepository.saveAll(List.of(product2Description1, product2Description2));

        };
    }

}
