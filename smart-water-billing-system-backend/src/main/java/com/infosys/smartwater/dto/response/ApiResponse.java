package com.infosys.smartwater.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Generic API response wrapper used by all REST endpoints.
 *
 * <p>Every controller response is wrapped in this object to provide
 * a consistent JSON envelope:
 * <pre>
 * {
 *   "success"   : true,
 *   "message"   : "Apartment retrieved successfully",
 *   "data"      : { ... },
 *   "timestamp" : "2026-07-23T21:23:23.123",
 *   "statusCode": 200
 * }
 * </pre>
 *
 * <p>{@code data} is omitted ({@code @JsonInclude(NON_NULL)}) from error
 * responses so clients never see a {@code null} field.
 *
 * @param <T> the type of the response payload
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API response envelope returned by all endpoints")
public class ApiResponse<T> {

    @Schema(description = "Indicates whether the request was processed successfully",
            example = "true")
    private boolean success;

    @Schema(description = "Human-readable message describing the outcome",
            example = "Apartment created successfully")
    private String message;

    @Schema(description = "Response payload — null for error responses")
    private T data;

    @Schema(description = "Server-side timestamp of the response",
            example = "2026-07-23T21:23:23.123")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Schema(description = "HTTP status code mirrored in the response body",
            example = "200")
    private int statusCode;

    // -------------------------------------------------------------------------
    // Static factory methods — success
    // -------------------------------------------------------------------------

    /**
     * Creates a successful response with a data payload.
     *
     * @param message    human-readable success message
     * @param data       the response payload
     * @param statusCode the HTTP status code (e.g., 200, 201)
     * @param <T>        payload type
     * @return a populated {@code ApiResponse}
     */
    public static <T> ApiResponse<T> success(String message, T data, int statusCode) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .statusCode(statusCode)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a successful response with no data payload (e.g., DELETE operations).
     *
     * @param message    human-readable success message
     * @param statusCode the HTTP status code
     * @param <T>        payload type (inferred as Void)
     * @return a {@code ApiResponse} with {@code data = null}
     */
    public static <T> ApiResponse<T> success(String message, int statusCode) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .statusCode(statusCode)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // -------------------------------------------------------------------------
    // Static factory methods — error
    // -------------------------------------------------------------------------

    /**
     * Creates an error response without a data payload.
     *
     * @param message    human-readable error message
     * @param statusCode the HTTP error status code (e.g., 400, 404, 500)
     * @param <T>        payload type (inferred as Void)
     * @return an error {@code ApiResponse}
     */
    public static <T> ApiResponse<T> error(String message, int statusCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .statusCode(statusCode)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates an error response with a data payload (e.g., validation error details).
     *
     * @param message    human-readable error message
     * @param data       the error detail payload (e.g., field validation errors map)
     * @param statusCode the HTTP error status code
     * @param <T>        payload type
     * @return an error {@code ApiResponse} with error details
     */
    public static <T> ApiResponse<T> error(String message, T data, int statusCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(data)
                .statusCode(statusCode)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
