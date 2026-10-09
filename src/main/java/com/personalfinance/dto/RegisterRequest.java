package com.personalfinance.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for user registration.
 *
 * <p>The username must be a valid email address, the password must satisfy the
 * system password policy (8-100 characters) and the phone number must be a
 * valid contact number (optional leading {@code +}, 7-18 digits optionally
 * separated by spaces, dashes or parentheses).</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    /** Username of the user; must be a valid, unique email address. */
    @NotBlank(message = "Username (email) is required")
    @Email(message = "Username must be a valid email address")
    private String username;

    /** Password of the user; must be between 8 and 100 characters long. */
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters long")
    private String password;

    /** Full name of the user. */
    @NotBlank(message = "Full name is required")
    private String fullName;

    /** Contact phone number of the user (e.g. {@code +1234567890}). */
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9()\\-\\s]{7,18}$", message = "Phone number must be a valid contact number")
    private String phoneNumber;
}
