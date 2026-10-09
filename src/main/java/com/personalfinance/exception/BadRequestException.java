package com.personalfinance.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown for invalid input or malformed requests.
 *
 * <p>Mapped to HTTP 400 Bad Request by {@link GlobalExceptionHandler}.</p>
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {
    /**
     * Creates a new exception with the given detail message.
     *
     * @param message the detail message
     */
    public BadRequestException(String message) {
        super(message);
    }
}
