package com.ihome24.ihome24.exception;

import lombok.Getter;

/**
 * Slug уже занят другим товаром (HTTP 409).
 */
@Getter
public class ProductSlugConflictException extends RuntimeException {

    private final Long conflictingProductId;

    public ProductSlugConflictException(String message) {
        this(message, null);
    }

    public ProductSlugConflictException(String message, Long conflictingProductId) {
        super(message);
        this.conflictingProductId = conflictingProductId;
    }
}
