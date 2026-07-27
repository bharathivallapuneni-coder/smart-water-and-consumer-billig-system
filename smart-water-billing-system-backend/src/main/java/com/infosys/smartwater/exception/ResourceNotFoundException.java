package com.infosys.smartwater.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested resource cannot be found in the database.
 *
 * <p>Maps to HTTP {@code 404 Not Found}.
 *
 * <p>Usage:
 * <pre>
 *   apartmentRepository.findById(id)
 *       .orElseThrow(() -> new ResourceNotFoundException("Apartment", "id", id));
 *   // Produces: "Apartment not found with id: '3fa85f64-...'"
 * </pre>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends BusinessException {

    /**
     * Constructs a {@code ResourceNotFoundException} with a descriptive message.
     *
     * @param resourceName the name of the entity type (e.g., "Apartment")
     * @param fieldName    the field used for lookup (e.g., "id", "email")
     * @param fieldValue   the value that was searched for
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(
            String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue),
            HttpStatus.NOT_FOUND
        );
    }
}
