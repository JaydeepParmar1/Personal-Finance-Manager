package com.personalfinance.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for user login.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    /** Username (email address) of the user. */
    @NotBlank(message = "Username is required")
    private String username;

    /** Password of the user. */
    @NotBlank(message = "Password is required")
    private String password;
}
