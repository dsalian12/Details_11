package com.salian.productapi.web;

import com.salian.productapi.service.ProductService;
import com.salian.productapi.web.dto.ProductRequest;
import com.salian.productapi.web.dto.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "CRUD operations for products")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "List products (paginated)")
    public Page<ProductResponse> list(@ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return productService.findAll(pageable).map(ProductResponse::from);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a product by id")
    public ProductResponse get(@PathVariable Long id) {
        return ProductResponse.from(productService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Create a product")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        ProductResponse response = ProductResponse.from(productService.create(request));
        return ResponseEntity.created(URI.create("/api/v1/products/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace an existing product")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return ProductResponse.from(productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
