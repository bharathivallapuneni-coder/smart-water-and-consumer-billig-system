package com.infosys.smartwater.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a CSV file cannot be parsed or contains unrecoverable structural errors.
 *
 * <p>Maps to HTTP {@code 400 Bad Request}.
 *
 * <p>This exception is raised for file-level errors (e.g., wrong format, empty file,
 * I/O failures). Row-level errors during import are tracked individually in
 * {@code CsvImportSummaryResponse.CsvRowError} and do not throw this exception.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CsvProcessingException extends BusinessException {

    /**
     * Constructs a {@code CsvProcessingException} with the given message.
     *
     * @param message description of the file-level CSV error
     */
    public CsvProcessingException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

    /**
     * Constructs a {@code CsvProcessingException} wrapping an underlying cause.
     *
     * @param message description of the file-level CSV error
     * @param cause   the underlying I/O or parsing exception
     */
    public CsvProcessingException(String message, Throwable cause) {
        super(message, HttpStatus.BAD_REQUEST, cause);
    }
}
