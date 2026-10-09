package com.personalfinance.service;

import com.personalfinance.dto.MonthlyReportResponse;
import com.personalfinance.dto.YearlyReportResponse;
import com.personalfinance.entity.CategoryType;
import com.personalfinance.entity.Transaction;
import com.personalfinance.entity.User;
import com.personalfinance.exception.BadRequestException;
import com.personalfinance.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service layer for generating financial reports and analytics.
 *
 * <p>Reports aggregate the transactions of the currently authenticated user
 * per category, providing monthly and yearly overviews of income, expenses
 * and net savings.</p>
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final AuthService authService;

    /**
     * Generates a monthly financial report for the currently authenticated user.
     *
     * @param year  the report year (1-9999)
     * @param month the report month (1-12)
     * @return the monthly report with income/expenses per category and net savings
     * @throws BadRequestException if the month or year is out of range
     */
    @Transactional(readOnly = true)
    public MonthlyReportResponse getMonthlyReport(int year, int month) {
        User currentUser = authService.getCurrentUser();

        if (year < 1 || year > 9999) {
            throw new BadRequestException("Year must be between 1 and 9999");
        }
        if (month < 1 || month > 12) {
            throw new BadRequestException("Month must be between 1 and 12");
        }

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Transaction> transactions = transactionRepository.findByUserAndDateBetween(currentUser, startDate, endDate);

        Map<String, BigDecimal> totalIncome = new HashMap<>();
        Map<String, BigDecimal> totalExpenses = new HashMap<>();

        BigDecimal sumIncome = BigDecimal.ZERO;
        BigDecimal sumExpenses = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            String categoryName = t.getCategory().getName();
            BigDecimal amount = t.getAmount();

            if (t.getCategory().getType() == CategoryType.INCOME) {
                totalIncome.put(categoryName, totalIncome.getOrDefault(categoryName, BigDecimal.ZERO).add(amount));
                sumIncome = sumIncome.add(amount);
            } else if (t.getCategory().getType() == CategoryType.EXPENSE) {
                totalExpenses.put(categoryName, totalExpenses.getOrDefault(categoryName, BigDecimal.ZERO).add(amount));
                sumExpenses = sumExpenses.add(amount);
            }
        }

        BigDecimal netSavings = sumIncome.subtract(sumExpenses);

        return MonthlyReportResponse.builder()
                .month(month)
                .year(year)
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netSavings(netSavings)
                .build();
    }

    /**
     * Generates a yearly financial report for the currently authenticated user by
     * aggregating all transactions of the specified year.
     *
     * @param year the report year (1-9999)
     * @return the yearly report with income/expenses per category and net savings
     * @throws BadRequestException if the year is out of range
     */
    @Transactional(readOnly = true)
    public YearlyReportResponse getYearlyReport(int year) {
        User currentUser = authService.getCurrentUser();

        if (year < 1 || year > 9999) {
            throw new BadRequestException("Year must be between 1 and 9999");
        }

        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        List<Transaction> transactions = transactionRepository.findByUserAndDateBetween(currentUser, startDate, endDate);

        Map<String, BigDecimal> totalIncome = new HashMap<>();
        Map<String, BigDecimal> totalExpenses = new HashMap<>();

        BigDecimal sumIncome = BigDecimal.ZERO;
        BigDecimal sumExpenses = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            String categoryName = t.getCategory().getName();
            BigDecimal amount = t.getAmount();

            if (t.getCategory().getType() == CategoryType.INCOME) {
                totalIncome.put(categoryName, totalIncome.getOrDefault(categoryName, BigDecimal.ZERO).add(amount));
                sumIncome = sumIncome.add(amount);
            } else if (t.getCategory().getType() == CategoryType.EXPENSE) {
                totalExpenses.put(categoryName, totalExpenses.getOrDefault(categoryName, BigDecimal.ZERO).add(amount));
                sumExpenses = sumExpenses.add(amount);
            }
        }

        BigDecimal netSavings = sumIncome.subtract(sumExpenses);

        return YearlyReportResponse.builder()
                .year(year)
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netSavings(netSavings)
                .build();
    }
}
