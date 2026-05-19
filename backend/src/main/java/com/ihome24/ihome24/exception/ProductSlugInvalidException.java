package com.ihome24.ihome24.exception;

/**
 * Недопустимый формат slug (HTTP 422).
 */
public class ProductSlugInvalidException extends RuntimeException {

    public ProductSlugInvalidException(String message) {
        super(message);
    }
}
