package com.salian.productapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.salian.productapi.domain.Product;
import com.salian.productapi.exception.DuplicateSkuException;
import com.salian.productapi.exception.ProductNotFoundException;
import com.salian.productapi.repository.ProductRepository;
import com.salian.productapi.web.dto.ProductRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Logger serviceLogger;

    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void attachLogAppender() {
        serviceLogger = (Logger) LoggerFactory.getLogger(ProductService.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        serviceLogger.addAppender(logAppender);
    }

    @AfterEach
    void detachLogAppender() {
        serviceLogger.detachAppender(logAppender);
    }

    private List<ILoggingEvent> logsAt(Level level) {
        return logAppender.list.stream().filter(event -> event.getLevel() == level).toList();
    }

    private static ProductRequest request(String sku) {
        return new ProductRequest(sku, "Keyboard", "87-key", new BigDecimal("129.99"), 25);
    }

    @Test
    void createPersistsProduct() {
        when(productRepository.existsBySku("SKU-1")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product created = productService.create(request("SKU-1"));

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getSku()).isEqualTo("SKU-1");
        assertThat(created.getPrice()).isEqualByComparingTo("129.99");
        assertThat(created.getQuantity()).isEqualTo(25);
        assertThat(logsAt(Level.INFO))
                .extracting(ILoggingEvent::getFormattedMessage)
                .anyMatch(message -> message.startsWith("Created product"));
    }

    @Test
    void createRejectsDuplicateSku() {
        when(productRepository.existsBySku("SKU-1")).thenReturn(true);

        assertThatThrownBy(() -> productService.create(request("SKU-1")))
                .isInstanceOf(DuplicateSkuException.class);
        verify(productRepository, never()).save(any());
        assertThat(logsAt(Level.WARN))
                .extracting(ILoggingEvent::getFormattedMessage)
                .containsExactly("Rejected product creation, sku already exists sku=SKU-1");
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(productRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(42L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("42");
        assertThat(logsAt(Level.WARN))
                .extracting(ILoggingEvent::getFormattedMessage)
                .containsExactly("Product not found id=42");
    }

    @Test
    void updateMutatesExistingProduct() {
        Product existing = Product.builder()
                .id(1L)
                .sku("SKU-1")
                .name("Old")
                .price(new BigDecimal("10.00"))
                .quantity(1)
                .build();
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product updated = productService.update(1L, request("SKU-1"));

        assertThat(updated.getName()).isEqualTo("Keyboard");
        assertThat(updated.getQuantity()).isEqualTo(25);
    }

    @Test
    void updateRejectsSkuTakenByAnotherProduct() {
        Product existing = Product.builder()
                .id(1L)
                .sku("SKU-1")
                .name("Old")
                .price(new BigDecimal("10.00"))
                .quantity(1)
                .build();
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.existsBySku("SKU-2")).thenReturn(true);

        assertThatThrownBy(() -> productService.update(1L, request("SKU-2")))
                .isInstanceOf(DuplicateSkuException.class);
    }

    @Test
    void deleteRemovesProduct() {
        Product existing = Product.builder().id(1L).sku("SKU-1").build();
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));

        productService.delete(1L);

        verify(productRepository).delete(existing);
    }

    @Test
    void createThrowsDuplicateSkuOnUniqueConstraintViolation() {
        when(productRepository.existsBySku("SKU-1")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new DataIntegrityViolationException("duplicate")).when(productRepository).flush();

        assertThatThrownBy(() -> productService.create(request("SKU-1")))
                .isInstanceOf(DuplicateSkuException.class);
    }

    @Test
    void updateThrowsDuplicateSkuOnUniqueConstraintViolation() {
        Product existing = Product.builder()
                .id(1L)
                .sku("SKU-1")
                .name("Old")
                .price(new BigDecimal("10.00"))
                .quantity(1)
                .build();
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.existsBySku("SKU-2")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new DataIntegrityViolationException("duplicate")).when(productRepository).flush();

        assertThatThrownBy(() -> productService.update(1L, request("SKU-2")))
                .isInstanceOf(DuplicateSkuException.class);
    }
}
