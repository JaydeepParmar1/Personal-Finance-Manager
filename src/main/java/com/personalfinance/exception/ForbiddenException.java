package com.personalfinance.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a user attempts to access data owned by another user.
 *
 * <p>Mapped to HTTP 403 Forbidden by {@link GlobalExceptionHandler}.</p>
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException {
    /**
     * Creates a new exception with the given detail message.
     *
     * @param message the detail message
     */
    public ForbiddenException(String message) {
        super(message);
    }
}
