package com.personalfinance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Response DTO representing a monthly financial report.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyReportResponse {
    /** Month of the report (1-12). */
    private Integer month;
    /** Year of the report. */
    private Integer year;
    /** Total income aggregated per category name. */
    private Map<String, BigDecimal> totalIncome;
    /** Total expenses aggregated per category name. */
    private Map<String, BigDecimal> totalExpenses;
    /** Net savings (total income minus total expenses). */
    private BigDecimal netSavings;
}
