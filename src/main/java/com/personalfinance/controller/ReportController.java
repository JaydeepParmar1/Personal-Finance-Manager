package com.personalfinance.controller;

import com.personalfinance.dto.MonthlyReportResponse;
import com.personalfinance.dto.YearlyReportResponse;
import com.personalfinance.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for financial reports and analytics.
 *
 * <p>Reports aggregate the transactions of the currently authenticated user
 * per category for a given month/year or an entire year.</p>
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * Generates a monthly financial report.
     *
     * @param year  the report year
     * @param month the report month (1-12)
     * @return the monthly report with HTTP 200
     */
    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<MonthlyReportResponse> getMonthlyReport(
            @PathVariable int year,
            @PathVariable int month) {
        MonthlyReportResponse response = reportService.getMonthlyReport(year, month);
        return ResponseEntity.ok(response);
    }

    /**
     * Generates a yearly financial report.
     *
     * @param year the report year
     * @return the yearly report with HTTP 200
     */
    @GetMapping("/yearly/{year}")
    public ResponseEntity<YearlyReportResponse> getYearlyReport(
            @PathVariable int year) {
        YearlyReportResponse response = reportService.getYearlyReport(year);
        return ResponseEntity.ok(response);
    }
}
