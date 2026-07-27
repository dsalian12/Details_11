package com.salian.productapi.service;

import com.salian.productapi.domain.Product;
import com.salian.productapi.exception.DuplicateSkuException;
import com.salian.productapi.exception.ProductNotFoundException;
import com.salian.productapi.repository.ProductRepository;
import com.salian.productapi.web.dto.ProductRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Product findById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Transactional
    public Product create(ProductRequest request) {
        if (productRepository.existsBySku(request.sku())) {
            throw new DuplicateSkuException(request.sku());
        }
        Product product = Product.builder()
                .sku(request.sku())
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .quantity(request.quantity())
                .build();
        return productRepository.save(product);
    }

    @Transactional
    public Product update(Long id, ProductRequest request) {
        Product product = findById(id);
        if (!product.getSku().equals(request.sku()) && productRepository.existsBySku(request.sku())) {
            throw new DuplicateSkuException(request.sku());
        }
        product.setSku(request.sku());
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setQuantity(request.quantity());
        return productRepository.save(product);
    }

    @Transactional
    public void delete(Long id) {
        Product product = findById(id);
        productRepository.delete(product);
    }
}
