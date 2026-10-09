package com.personalfinance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for creating a savings goal.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalRequest {

    /** Descriptive name of the goal. */
    @NotBlank(message = "Goal name is required")
    private String goalName;

    /** Target amount to save; must be a positive decimal value. */
    @NotNull(message = "Target amount is required")
    @DecimalMin(value = "0.01", message = "Target amount must be a positive decimal value")
    private BigDecimal targetAmount;

    /** Target date by which the goal should be reached; must be a future date. */
    @NotNull(message = "Target date is required")
    @Future(message = "Target date must be a future date")
    private LocalDate targetDate;

    /** Start date of the goal; defaults to the creation date when omitted. */
    private LocalDate startDate;
}
