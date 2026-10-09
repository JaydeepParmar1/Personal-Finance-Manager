package com.personalfinance.controller;

import com.personalfinance.dto.*;
import com.personalfinance.entity.CategoryType;
import com.personalfinance.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * REST controller for managing financial transactions.
 *
 * <p>All endpoints require authentication. Every transaction is scoped to the
 * currently authenticated user, so users can only access their own data.</p>
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Creates a new transaction for the authenticated user.
     *
     * @param request the transaction details (amount, date, category, description)
     * @return the created transaction with HTTP 201
     */
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody TransactionRequest request) {
        TransactionResponse response = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all transactions of the authenticated user, sorted by newest first,
     * optionally filtered by date range, category (id or name) and transaction type.
     *
     * @param startDate    the start of the date range (inclusive), optional
     * @param endDate      the end of the date range (inclusive), optional
     * @param categoryId   the id of the category to filter by, optional
     * @param categoryName the name of the category to filter by, optional
     * @param type         the transaction type (INCOME/EXPENSE) to filter by, optional
     * @return the list of matching transactions with HTTP 200
     */
    @GetMapping
    public ResponseEntity<TransactionListResponse> getTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) CategoryType type) {
        TransactionListResponse response = transactionService.getTransactions(startDate, endDate, categoryId, categoryName, type);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates an existing transaction of the authenticated user. The date field
     * is immutable; any attempt to modify it is rejected with HTTP 400.
     *
     * @param id      the id of the transaction to update
     * @param request the fields to update (amount, category, description)
     * @return the updated transaction with HTTP 200
     */
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionUpdateRequest request) {
        TransactionResponse response = transactionService.updateTransaction(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a transaction of the authenticated user.
     *
     * @param id the id of the transaction to delete
     * @return a confirmation message with HTTP 200
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteTransaction(@PathVariable Long id) {
        MessageResponse response = transactionService.deleteTransaction(id);
        return ResponseEntity.ok(response);
    }
}
