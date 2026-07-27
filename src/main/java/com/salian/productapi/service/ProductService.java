package com.salian.productapi.service;

import com.salian.productapi.domain.Product;
import com.salian.productapi.exception.DuplicateSkuException;
import com.salian.productapi.exception.ProductNotFoundException;
import com.salian.productapi.repository.ProductRepository;
import com.salian.productapi.web.dto.ProductRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    public Page<Product> findAll(Pageable pageable) {
        log.info("Listing products page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Product> products = productRepository.findAll(pageable);
        log.info("Retrieved {} of {} products", products.getNumberOfElements(), products.getTotalElements());
        return products;
    }

    public Product findById(Long id) {
        log.info("Fetching product id={}", id);
        return productRepository.findById(id).orElseThrow(() -> {
            log.warn("Product not found id={}", id);
            return new ProductNotFoundException(id);
        });
    }

    @Transactional
    public Product create(ProductRequest request) {
        log.info("Creating product sku={}", request.sku());
        if (productRepository.existsBySku(request.sku())) {
            log.warn("Rejected product creation, sku already exists sku={}", request.sku());
            throw new DuplicateSkuException(request.sku());
        }
        Product product = Product.builder()
                .sku(request.sku())
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .quantity(request.quantity())
                .build();
        Product saved = productRepository.save(product);
        log.info("Created product id={} sku={}", saved.getId(), saved.getSku());
        return saved;
    }

    @Transactional
    public Product update(Long id, ProductRequest request) {
        log.info("Updating product id={}", id);
        Product product = findById(id);
        if (!product.getSku().equals(request.sku()) && productRepository.existsBySku(request.sku())) {
            log.warn("Rejected product update id={}, sku already exists sku={}", id, request.sku());
            throw new DuplicateSkuException(request.sku());
        }
        product.setSku(request.sku());
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setQuantity(request.quantity());
        Product saved = productRepository.save(product);
        log.info("Updated product id={} sku={} quantity={}", saved.getId(), saved.getSku(), saved.getQuantity());
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting product id={}", id);
        Product product = findById(id);
        productRepository.delete(product);
        log.info("Deleted product id={} sku={}", id, product.getSku());
    }
}
