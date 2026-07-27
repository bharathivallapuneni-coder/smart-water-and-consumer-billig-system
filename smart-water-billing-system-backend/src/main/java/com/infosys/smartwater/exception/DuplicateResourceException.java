package com.infosys.smartwater.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when attempting to create a resource that violates a unique constraint.
 *
 * <p>Maps to HTTP {@code 409 Conflict}.
 *
 * <p>Usage:
 * <pre>
 *   if (repository.existsByEmail(email)) {
 *       throw new DuplicateResourceException("User", "email", email);
 *   }
 *   // Produces: "User already exists with email: 'john@example.com'"
 * </pre>
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateResourceException extends BusinessException {

    /**
     * Constructs a {@code DuplicateResourceException} with a descriptive message.
     *
     * @param resourceName the name of the entity type (e.g., "User")
     * @param fieldName    the unique field that caused the conflict (e.g., "email")
     * @param fieldValue   the duplicate value
     */
    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(
            String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue),
            HttpStatus.CONFLICT
        );
    }
}
