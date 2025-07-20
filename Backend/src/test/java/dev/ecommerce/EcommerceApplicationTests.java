package dev.ecommerce;

import dev.ecommerce.product.DTO.ProductLineDTO;
import dev.ecommerce.product.DTO.ProductMapper;
import dev.ecommerce.product.constant.ContentType;
import dev.ecommerce.product.entity.ProductLine;
import dev.ecommerce.product.entity.ProductLineDescription;
import dev.ecommerce.product.entity.ProductLineMedia;
import dev.ecommerce.product.repository.ProductLineRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

@DataJpaTest
@ActiveProfiles("test") // use H2 config here
class EcommerceApplicationTests {

    @Autowired
    private ProductLineRepository productLineRepository;

    private final ProductMapper productMapper = Mappers.getMapper(ProductMapper.class);

    @Test
    void testFindProductLineWithDescriptionsAndMedia() {
        ProductLine productLine = new ProductLine("Product line name");

        ProductLineDescription description = new ProductLineDescription(productLine, ContentType.IMAGE, "desc image", 0);
        ProductLineMedia media = new ProductLineMedia(productLine, ContentType.IMAGE, "media image", 0);

        productLine.getDescriptions().add(description);
        productLine.getMedia().add(media);

        productLineRepository.save(productLine);

        Optional<ProductLine> resultOpt = productLineRepository.findById(productLine.getId());
        Assertions.assertTrue(resultOpt.isPresent());

        ProductLine result = resultOpt.get();

        Assertions.assertEquals(1, result.getDescriptions().size());
        Assertions.assertEquals("desc image", result.getDescriptions().getFirst().getContent());

        Assertions.assertEquals(1, result.getMedia().size());
        Assertions.assertEquals("media image", result.getMedia().getFirst().getContent());
    }

    @Test
    void testProductLineMapper() {
        ProductLine productLine = new ProductLine("Product line name");
        ProductLineDescription description = new ProductLineDescription(
                productLine,
                ContentType.IMAGE,
                "some image name",
                0
        );

        ProductLineMedia media = new ProductLineMedia(
                productLine,
                ContentType.IMAGE,
                "some image name",
                0
        );

        productLine.getDescriptions().add(description);
        productLine.getMedia().add(media);

        ProductLineDTO productLineDTO = productMapper.toProductLineDTO(productLine);
        Assertions.assertEquals(productLine.getName(), productLineDTO.getName());
        Assertions.assertEquals(productLine.getDescriptions().size(), productLineDTO.getDescriptions().size());
        Assertions.assertEquals(productLine.getMedia().size(), productLineDTO.getMedia().size());
        Assertions.assertEquals(
                productLine.getDescriptions().getFirst().getContent(),
                productLineDTO.getDescriptions().getFirst().content()
        );

    }



}
