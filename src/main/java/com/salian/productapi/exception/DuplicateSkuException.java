package com.salian.productapi.exception;

public class DuplicateSkuException extends RuntimeException {

    public DuplicateSkuException(String sku) {
        super("Product already exists with sku: " + sku);
    }
}
