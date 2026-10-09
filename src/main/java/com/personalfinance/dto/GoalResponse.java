package com.personalfinance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO representing a savings goal with its current progress.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalResponse {
    /** Unique identifier of the goal. */
    private Long id;
    /** Descriptive name of the goal. */
    private String goalName;
    /** Target amount to be saved. */
    private BigDecimal targetAmount;
    /** Date by which the goal should be reached. */
    private LocalDate targetDate;
    /** Date from which progress is calculated. */
    private LocalDate startDate;
    /** Current progress (total income minus total expenses since the start date). */
    private BigDecimal currentProgress;
    /** Progress as a percentage of the target amount. */
    private Double progressPercentage;
    /** Amount still required to reach the target. */
    private BigDecimal remainingAmount;
}
