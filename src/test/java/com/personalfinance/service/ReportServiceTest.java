package com.personalfinance.service;

import com.personalfinance.dto.MonthlyReportResponse;
import com.personalfinance.dto.YearlyReportResponse;
import com.personalfinance.entity.Category;
import com.personalfinance.entity.CategoryType;
import com.personalfinance.entity.Transaction;
import com.personalfinance.entity.User;
import com.personalfinance.exception.BadRequestException;
import com.personalfinance.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private ReportService reportService;

    private User testUser;
    private Transaction salaryTx;
    private Transaction foodTx;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).username("user@example.com").build();

        Category salary = Category.builder().name("Salary").type(CategoryType.INCOME).build();
        Category food = Category.builder().name("Food").type(CategoryType.EXPENSE).build();

        salaryTx = Transaction.builder()
                .amount(new BigDecimal("3000.00"))
                .category(salary)
                .date(LocalDate.of(2024, 1, 15))
                .build();

        foodTx = Transaction.builder()
                .amount(new BigDecimal("400.00"))
                .category(food)
                .date(LocalDate.of(2024, 1, 20))
                .build();
    }

    @Test
    void getMonthlyReport_Success() {
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(transactionRepository.findByUserAndDateBetween(eq(testUser), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(salaryTx, foodTx));

        MonthlyReportResponse response = reportService.getMonthlyReport(2024, 1);

        assertNotNull(response);
        assertEquals(1, response.getMonth());
        assertEquals(2024, response.getYear());
        assertEquals(new BigDecimal("3000.00"), response.getTotalIncome().get("Salary"));
        assertEquals(new BigDecimal("400.00"), response.getTotalExpenses().get("Food"));
        assertEquals(new BigDecimal("2600.00"), response.getNetSavings());
    }

    @Test
    void getYearlyReport_Success() {
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(transactionRepository.findByUserAndDateBetween(eq(testUser), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(salaryTx, foodTx));

        YearlyReportResponse response = reportService.getYearlyReport(2024);

        assertNotNull(response);
        assertEquals(2024, response.getYear());
        assertEquals(new BigDecimal("3000.00"), response.getTotalIncome().get("Salary"));
        assertEquals(new BigDecimal("400.00"), response.getTotalExpenses().get("Food"));
        assertEquals(new BigDecimal("2600.00"), response.getNetSavings());
    }

    @Test
    void getMonthlyReport_InvalidMonth_ThrowsBadRequestException() {
        when(authService.getCurrentUser()).thenReturn(testUser);

        assertThrows(BadRequestException.class, () -> reportService.getMonthlyReport(2024, 13));
    }

    @Test
    void getMonthlyReport_InvalidYear_ThrowsBadRequestException() {
        when(authService.getCurrentUser()).thenReturn(testUser);

        assertThrows(BadRequestException.class, () -> reportService.getMonthlyReport(10000, 1));
    }

    @Test
    void getYearlyReport_InvalidYear_ThrowsBadRequestException() {
        when(authService.getCurrentUser()).thenReturn(testUser);

        assertThrows(BadRequestException.class, () -> reportService.getYearlyReport(0));
    }
}
