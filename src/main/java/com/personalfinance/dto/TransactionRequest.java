package com.personalfinance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for creating a financial transaction.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequest {

    /** Transaction amount; must be a positive decimal value. */
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be a positive decimal value")
    private BigDecimal amount;

    /** Transaction date in YYYY-MM-DD format; cannot be a future date. */
    @NotNull(message = "Date is required")
    @PastOrPresent(message = "Transaction date cannot be a future date")
    private LocalDate date;

    /** Name of the category the transaction belongs to. */
    @NotBlank(message = "Category name is required")
    private String category;

    /** Optional description of the transaction. */
    private String description;
}
