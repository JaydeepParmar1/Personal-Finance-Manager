package com.personalfinance.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a request conflicts with existing data
 * (e.g. duplicate usernames or category names).
 *
 * <p>Mapped to HTTP 409 Conflict by {@link GlobalExceptionHandler}.</p>
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends RuntimeException {
    /**
     * Creates a new exception with the given detail message.
     *
     * @param message the detail message
     */
    public ConflictException(String message) {
        super(message);
    }
}
