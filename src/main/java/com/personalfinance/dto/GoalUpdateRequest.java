package com.personalfinance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for updating a savings goal.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalUpdateRequest {

    /** New name of the goal. */
    private String goalName;

    /** New target amount; must be a positive decimal value. */
    @DecimalMin(value = "0.01", message = "Target amount must be a positive decimal value")
    private BigDecimal targetAmount;

    /** New target date; must be a future date. */
    @Future(message = "Target date must be a future date")
    private LocalDate targetDate;
}
