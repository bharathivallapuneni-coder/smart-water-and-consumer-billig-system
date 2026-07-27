package com.infosys.smartwater.exception;

import com.infosys.smartwater.dto.response.ApiResponse;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("GlobalExceptionHandler Unit Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("handleBusinessException - ResourceNotFoundException returns 404")
    void handleBusinessException_ResourceNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Apartment", "id", UUID.randomUUID());

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleBusinessException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(404, response.getBody().getStatusCode());
        assertTrue(response.getBody().getMessage().contains("Apartment not found"));
    }

    @Test
    @DisplayName("handleBusinessException - DuplicateResourceException returns 409")
    void handleBusinessException_DuplicateResource() {
        DuplicateResourceException ex = new DuplicateResourceException("User", "email", "test@example.com");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleBusinessException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(409, response.getBody().getStatusCode());
        assertTrue(response.getBody().getMessage().contains("already exists"));
    }

    @Test
    @DisplayName("handleBusinessException - InvalidOperationException returns 400")
    void handleBusinessException_InvalidOperation() {
        InvalidOperationException ex = new InvalidOperationException("Cannot delete apartment with active households");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleBusinessException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatusCode());
        assertEquals("Cannot delete apartment with active households", response.getBody().getMessage());
    }

    @Test
    @DisplayName("handleBusinessException - CsvProcessingException returns 400")
    void handleBusinessException_CsvProcessing() {
        CsvProcessingException ex = new CsvProcessingException("CSV header invalid");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleBusinessException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("CSV header invalid", response.getBody().getMessage());
    }

    @Test
    @DisplayName("handleValidationException - MethodArgumentNotValidException returns 400 with field errors")
    void handleValidationException() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("apartmentRequest", "apartmentNumber", "Apartment number is required");
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ApiResponse<Map<String, String>>> response = exceptionHandler.handleValidationException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertEquals("Apartment number is required", response.getBody().getData().get("apartmentNumber"));
    }

    @Test
    @DisplayName("handleJwtException - JwtException returns 401 Unauthorized")
    void handleJwtException() {
        JwtException ex = new MalformedJwtException("Token string is compact JWT");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleJwtException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getStatusCode());
        assertTrue(response.getBody().getMessage().contains("Invalid or expired JWT token"));
    }

    @Test
    @DisplayName("handleAccessDeniedException - AccessDeniedException returns 403 Forbidden")
    void handleAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("Access is denied");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleAccessDeniedException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().getStatusCode());
        assertTrue(response.getBody().getMessage().contains("Access denied"));
    }

    @Test
    @DisplayName("handleBadCredentialsException - BadCredentialsException returns 401 Unauthorized")
    void handleBadCredentialsException() {
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleBadCredentialsException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getStatusCode());
        assertEquals("Invalid email or password", response.getBody().getMessage());
    }

    @Test
    @DisplayName("handleMaxUploadSizeExceededException - returns 400 Bad Request")
    void handleMaxUploadSizeExceededException() {
        MaxUploadSizeExceededException ex = new MaxUploadSizeExceededException(5000000L);

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleMaxUploadSizeExceededException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("Uploaded file size exceeds"));
    }

    @Test
    @DisplayName("handleHttpMessageNotReadable - returns 400 Bad Request")
    void handleHttpMessageNotReadable() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(ex.getMessage()).thenReturn("Required request body is missing");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleHttpMessageNotReadable(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Malformed JSON request body", response.getBody().getMessage());
    }

    @Test
    @DisplayName("handleMethodArgumentTypeMismatch - returns 400 Bad Request")
    void handleMethodArgumentTypeMismatch() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("id");
        when(ex.getMessage()).thenReturn("Failed to convert value");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleMethodArgumentTypeMismatch(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Parameter 'id' is invalid or of incorrect type", response.getBody().getMessage());
    }

    @Test
    @DisplayName("handleHttpRequestMethodNotSupported - returns 405 Method Not Allowed")
    void handleHttpRequestMethodNotSupported() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("POST", List.of("GET", "DELETE"));

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleHttpRequestMethodNotSupported(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("HTTP method 'POST' is not supported"));
    }

    @Test
    @DisplayName("handleNoHandlerFoundException - returns 404 Not Found")
    void handleNoHandlerFoundException() {
        NoHandlerFoundException ex = new NoHandlerFoundException("GET", "/api/v1/unknown", null);

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleNoHandlerFoundException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Requested resource or endpoint does not exist", response.getBody().getMessage());
    }

    @Test
    @DisplayName("handleDataIntegrityViolation - Unique constraint returns 409 Conflict")
    void handleDataIntegrityViolation_UniqueConstraint() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Constraint violation", new RuntimeException("duplicate key value violates unique constraint uq_apartments_number"));

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleDataIntegrityViolation(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatusCode());
        assertEquals("A record with matching unique criteria already exists", response.getBody().getMessage());
    }

    @Test
    @DisplayName("handleDataIntegrityViolation - General constraint returns 400 Bad Request")
    void handleDataIntegrityViolation_GeneralConstraint() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Column total_floors cannot be null");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleDataIntegrityViolation(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatusCode());
        assertEquals("Database constraint violation occurred", response.getBody().getMessage());
    }

    @Test
    @DisplayName("handleGlobalException - NullPointerException returns 500 Internal Server Error")
    void handleGlobalException() {
        NullPointerException ex = new NullPointerException("Null reference encountered");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleGlobalException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatusCode());
        assertEquals("An unexpected internal server error occurred", response.getBody().getMessage());
    }
}
