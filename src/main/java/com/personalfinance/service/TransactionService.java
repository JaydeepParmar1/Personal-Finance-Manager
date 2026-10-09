package com.personalfinance.service;

import com.personalfinance.dto.*;
import com.personalfinance.entity.Category;
import com.personalfinance.entity.CategoryType;
import com.personalfinance.entity.Transaction;
import com.personalfinance.entity.User;
import com.personalfinance.exception.BadRequestException;
import com.personalfinance.exception.ForbiddenException;
import com.personalfinance.exception.ResourceNotFoundException;
import com.personalfinance.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for managing financial transactions.
 *
 * <p>Enforces business rules such as positive amounts, non-future dates, valid
 * category references and strict per-user data isolation. Accessing another
 * user's transaction results in a {@link ForbiddenException} (HTTP 403).</p>
 */
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;
    private final AuthService authService;

    /**
     * Creates a new financial transaction for the currently authenticated user.
     *
     * @param request the transaction details (amount, date, category, description)
     * @return the created transaction as a response DTO
     * @throws BadRequestException if the date is in the future or the category is invalid
     */
    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        User currentUser = authService.getCurrentUser();

        if (request.getDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Transaction date cannot be a future date");
        }

        Category category = categoryService.getCategoryByNameForUser(request.getCategory(), currentUser);

        Transaction transaction = Transaction.builder()
                .user(currentUser)
                .amount(request.getAmount())
                .date(request.getDate())
                .category(category)
                .description(request.getDescription())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);
    }

    /**
     * Retrieves all transactions of the currently authenticated user, sorted by
     * newest first, optionally filtered by date range, category and transaction type.
     *
     * @param startDate    the start of the date range (inclusive), may be {@code null}
     * @param endDate      the end of the date range (inclusive), may be {@code null}
     * @param categoryId   the category id to filter by, may be {@code null}
     * @param categoryName the category name to filter by, may be {@code null}
     * @param type         the transaction type to filter by, may be {@code null}
     * @return a list of transaction response DTOs
     */
    @Transactional(readOnly = true)
    public TransactionListResponse getTransactions(LocalDate startDate, LocalDate endDate, Long categoryId, String categoryName, CategoryType type) {
        User currentUser = authService.getCurrentUser();
        List<Transaction> list = transactionRepository.findFilteredTransactions(currentUser, startDate, endDate, categoryId, categoryName, type);

        List<TransactionResponse> responseList = list.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return TransactionListResponse.builder()
                .transactions(responseList)
                .build();
    }

    /**
     * Updates an existing transaction of the currently authenticated user.
     * The date field is immutable; any attempt to modify it is rejected.
     *
     * @param id      the id of the transaction to update
     * @param request the fields to update (amount, category, description)
     * @return the updated transaction as a response DTO
     * @throws ResourceNotFoundException if no transaction with the given id exists
     * @throws ForbiddenException        if the transaction belongs to another user
     * @throws BadRequestException       if the date field is modified or the category is invalid
     */
    @Transactional
    public TransactionResponse updateTransaction(Long id, TransactionUpdateRequest request) {
        User currentUser = authService.getCurrentUser();
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        if (!transaction.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Access denied: transaction belongs to another user");
        }

        // Check date immutability rule
        if (request.getDate() != null && !request.getDate().equals(transaction.getDate())) {
            throw new BadRequestException("Transaction date field cannot be modified");
        }

        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }

        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            Category category = categoryService.getCategoryByNameForUser(request.getCategory(), currentUser);
            transaction.setCategory(category);
        }

        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }

        Transaction updated = transactionRepository.save(transaction);
        return mapToResponse(updated);
    }

    /**
     * Deletes a transaction of the currently authenticated user.
     *
     * @param id the id of the transaction to delete
     * @return a confirmation message
     * @throws ResourceNotFoundException if no transaction with the given id exists
     * @throws ForbiddenException        if the transaction belongs to another user
     */
    @Transactional
    public MessageResponse deleteTransaction(Long id) {
        User currentUser = authService.getCurrentUser();
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        if (!transaction.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Access denied: transaction belongs to another user");
        }

        transactionRepository.delete(transaction);

        return MessageResponse.builder()
                .message("Transaction deleted successfully")
                .build();
    }

    /**
     * Maps a transaction entity to its response DTO.
     *
     * @param t the transaction entity
     * @return the corresponding response DTO
     */
    public TransactionResponse mapToResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .date(t.getDate())
                .category(t.getCategory().getName())
                .description(t.getDescription())
                .type(t.getCategory().getType())
                .build();
    }
}
