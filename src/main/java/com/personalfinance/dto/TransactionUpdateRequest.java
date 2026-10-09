package com.personalfinance.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for updating a financial transaction.
 *
 * <p>All fields are optional; the date field may only be present when it is
 * unchanged, since the date of a transaction is immutable.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionUpdateRequest {

    /** New transaction amount; must be a positive decimal value. */
    @DecimalMin(value = "0.01", message = "Amount must be a positive decimal value")
    private BigDecimal amount;

    /** New name of the category the transaction belongs to. */
    private String category;

    /** New description of the transaction. */
    private String description;

    /** May only be present when equal to the existing date (date is immutable). */
    private LocalDate date;
}
