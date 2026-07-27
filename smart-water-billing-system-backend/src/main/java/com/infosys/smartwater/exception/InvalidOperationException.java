package com.infosys.smartwater.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested operation violates a business rule or is in an invalid state.
 *
 * <p>Maps to HTTP {@code 400 Bad Request}.
 *
 * <p>Examples:
 * <ul>
 *   <li>Deleting an apartment that still has active households</li>
 *   <li>Generating a billing cycle for an inactive household</li>
 *   <li>Attempting to set a PAID billing cycle back to PENDING</li>
 *   <li>Creating a tariff plan whose dates overlap an existing active plan</li>
 *   <li>Meter reading less than the previous reading</li>
 * </ul>
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidOperationException extends BusinessException {

    /**
     * Constructs an {@code InvalidOperationException} with the given message.
     *
     * @param message human-readable description of the violated business rule
     */
    public InvalidOperationException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
