package com.infosys.smartwater.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Root exception for all domain-level business rule violations.
 *
 * <p>All application-specific exceptions extend this class so that
 * the {@code GlobalExceptionHandler} can catch them with a single handler
 * while still distinguishing subtypes for different HTTP status codes.
 *
 * <p>Subclasses declare their default HTTP status at construction time via
 * {@link #getStatus()}, allowing the exception handler to respond correctly
 * without a large if/else chain.
 */
@Getter
public abstract class BusinessException extends RuntimeException {

    /** HTTP status code this exception maps to. */
    private final HttpStatus status;

    protected BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    protected BusinessException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}
