package com.salian.productapi.web;

import com.salian.productapi.exception.DuplicateSkuException;
import com.salian.productapi.exception.ProductNotFoundException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ProblemDetail handleNotFound(ProductNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Product not found", ex.getMessage());
    }

    @ExceptionHandler(DuplicateSkuException.class)
    public ProblemDetail handleDuplicateSku(DuplicateSkuException ex) {
        return problem(HttpStatus.CONFLICT, "Duplicate SKU", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return problem(HttpStatus.BAD_REQUEST, "Validation failed", detail);
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        return problemDetail;
    }
}
