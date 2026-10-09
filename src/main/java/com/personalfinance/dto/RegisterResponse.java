package com.personalfinance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO returned after successful user registration.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponse {
    /** Confirmation message. */
    private String message;
    /** Id of the newly registered user. */
    private Long userId;
}
