package com.personalfinance.controller;

import com.personalfinance.dto.MonthlyReportResponse;
import com.personalfinance.dto.YearlyReportResponse;
import com.personalfinance.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @Test
    void getMonthlyReport_Success() throws Exception {
        MonthlyReportResponse response = MonthlyReportResponse.builder()
                .month(1)
                .year(2024)
                .totalIncome(Map.of("Salary", new BigDecimal("3000.00")))
                .totalExpenses(Map.of("Food", new BigDecimal("400.00")))
                .netSavings(new BigDecimal("2600.00"))
                .build();

        when(reportService.getMonthlyReport(2024, 1)).thenReturn(response);

        mockMvc.perform(get("/api/reports/monthly/2024/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value(1))
                .andExpect(jsonPath("$.year").value(2024))
                .andExpect(jsonPath("$.totalIncome.Salary").value(3000.00))
                .andExpect(jsonPath("$.netSavings").value(2600.00));
    }

    @Test
    void getYearlyReport_Success() throws Exception {
        YearlyReportResponse response = YearlyReportResponse.builder()
                .year(2024)
                .totalIncome(Map.of("Salary", new BigDecimal("36000.00")))
                .totalExpenses(Map.of("Food", new BigDecimal("4800.00")))
                .netSavings(new BigDecimal("31200.00"))
                .build();

        when(reportService.getYearlyReport(2024)).thenReturn(response);

        mockMvc.perform(get("/api/reports/yearly/2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2024))
                .andExpect(jsonPath("$.totalIncome.Salary").value(36000.00))
                .andExpect(jsonPath("$.netSavings").value(31200.00));
    }
}
