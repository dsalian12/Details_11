package com.salian.productapi.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.salian.productapi.domain.Product;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import com.salian.productapi.config.JpaAuditingConfig;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void savesAndFindsBySku() {
        productRepository.save(Product.builder()
                .sku("SKU-42")
                .name("Mouse")
                .price(new BigDecimal("49.50"))
                .quantity(10)
                .build());

        assertThat(productRepository.findBySku("SKU-42"))
                .hasValueSatisfying(product -> {
                    assertThat(product.getName()).isEqualTo("Mouse");
                    assertThat(product.getCreatedAt()).isNotNull();
                });
        assertThat(productRepository.existsBySku("SKU-unknown")).isFalse();
    }
}
