package com.personalfinance.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleBadRequest() {
        BadRequestException ex = new BadRequestException("Invalid input");
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleBadRequest(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad Request", response.getBody().get("error"));
        assertEquals("Invalid input", response.getBody().get("message"));
    }

    @Test
    void handleNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found");
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not Found", response.getBody().get("error"));
        assertEquals("Not found", response.getBody().get("message"));
    }

    @Test
    void handleConflict() {
        ConflictException ex = new ConflictException("Already exists");
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleConflict(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Conflict", response.getBody().get("error"));
        assertEquals("Already exists", response.getBody().get("message"));
    }

    @Test
    void handleForbidden() {
        ForbiddenException ex = new ForbiddenException("Access denied");
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleForbidden(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Forbidden", response.getBody().get("error"));
        assertEquals("Access denied", response.getBody().get("message"));
    }

    @Test
    void handleAuthenticationException() {
        BadCredentialsException ex = new BadCredentialsException("Bad creds");
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleAuthenticationException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Unauthorized", response.getBody().get("error"));
    }

    @Test
    void handleHttpMessageNotReadable() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Malformed JSON");
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleHttpMessageNotReadable(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad Request", response.getBody().get("error"));
        assertEquals("Malformed JSON or invalid field value", response.getBody().get("message"));
    }

    @Test
    void handleGenericException() {
        Exception ex = new Exception("Unexpected error");
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal Server Error", response.getBody().get("error"));
        assertEquals("Unexpected error", response.getBody().get("message"));
    }
}
