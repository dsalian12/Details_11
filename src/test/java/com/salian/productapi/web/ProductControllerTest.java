package com.salian.productapi.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salian.productapi.domain.Product;
import com.salian.productapi.exception.DuplicateSkuException;
import com.salian.productapi.exception.ProductNotFoundException;
import com.salian.productapi.service.ProductService;
import com.salian.productapi.web.dto.ProductRequest;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private static Product product() {
        return Product.builder()
                .id(1L)
                .sku("SKU-1")
                .name("Keyboard")
                .description("87-key")
                .price(new BigDecimal("129.99"))
                .quantity(25)
                .build();
    }

    @Test
    void listReturnsPageOfProducts() throws Exception {
        Page<Product> page = new PageImpl<>(java.util.List.of(product()));
        when(productService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].sku").value("SKU-1"))
                .andExpect(jsonPath("$.content[0].price").value(129.99));
    }

    @Test
    void getReturnsProduct() throws Exception {
        when(productService.findById(1L)).thenReturn(product());

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Keyboard"));
    }

    @Test
    void getReturnsProblemDetailWhenMissing() throws Exception {
        when(productService.findById(99L)).thenThrow(new ProductNotFoundException(99L));

        mockMvc.perform(get("/api/v1/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Product not found"));
    }

    @Test
    void createReturnsLocationHeader() throws Exception {
        ProductRequest request = new ProductRequest("SKU-1", "Keyboard", "87-key", new BigDecimal("129.99"), 25);
        when(productService.create(any(ProductRequest.class))).thenReturn(product());

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createRejectsInvalidPayload() throws Exception {
        ProductRequest request = new ProductRequest("", "", null, new BigDecimal("-1"), -5);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    @Test
    void createReturnsConflictOnDuplicateSku() throws Exception {
        ProductRequest request = new ProductRequest("SKU-1", "Keyboard", "87-key", new BigDecimal("129.99"), 25);
        when(productService.create(any(ProductRequest.class))).thenThrow(new DuplicateSkuException("SKU-1"));

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        doNothing().when(productService).delete(eq(1L));

        mockMvc.perform(delete("/api/v1/products/1")).andExpect(status().isNoContent());
    }
}
