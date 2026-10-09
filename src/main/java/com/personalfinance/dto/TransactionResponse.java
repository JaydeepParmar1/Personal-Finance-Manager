package com.personalfinance.dto;

import com.personalfinance.entity.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO representing a financial transaction.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {
    /** Unique identifier of the transaction. */
    private Long id;
    /** Transaction amount. */
    private BigDecimal amount;
    /** Transaction date. */
    private LocalDate date;
    /** Name of the category. */
    private String category;
    /** Description of the transaction. */
    private String description;
    /** Type of the transaction derived from its category (INCOME/EXPENSE). */
    private CategoryType type;
}
