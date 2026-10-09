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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private Category category;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).username("user@example.com").build();
        category = Category.builder().id(1L).name("Salary").type(CategoryType.INCOME).isCustom(false).build();
        transaction = Transaction.builder()
                .id(10L)
                .user(testUser)
                .amount(new BigDecimal("50000.00"))
                .date(LocalDate.now().minusDays(1))
                .category(category)
                .description("January Salary")
                .build();
    }

    @Test
    void createTransaction_Success() {
        TransactionRequest request = TransactionRequest.builder()
                .amount(new BigDecimal("50000.00"))
                .date(LocalDate.now().minusDays(1))
                .category("Salary")
                .description("January Salary")
                .build();

        when(authService.getCurrentUser()).thenReturn(testUser);
        when(categoryService.getCategoryByNameForUser("Salary", testUser)).thenReturn(category);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        TransactionResponse response = transactionService.createTransaction(request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(new BigDecimal("50000.00"), response.getAmount());
        assertEquals("Salary", response.getCategory());
        assertEquals(CategoryType.INCOME, response.getType());
    }

    @Test
    void createTransaction_FutureDate_ThrowsBadRequestException() {
        TransactionRequest request = TransactionRequest.builder()
                .amount(new BigDecimal("50000.00"))
                .date(LocalDate.now().plusDays(1))
                .category("Salary")
                .description("Future Salary")
                .build();

        when(authService.getCurrentUser()).thenReturn(testUser);

        assertThrows(BadRequestException.class, () -> transactionService.createTransaction(request));
    }

    @Test
    void getTransactions_Success() {
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(transactionRepository.findFilteredTransactions(testUser, null, null, null, null, null))
                .thenReturn(List.of(transaction));

        TransactionListResponse response = transactionService.getTransactions(null, null, null, null, null);

        assertNotNull(response);
        assertEquals(1, response.getTransactions().size());
        assertEquals(10L, response.getTransactions().get(0).getId());
    }

    @Test
    void updateTransaction_Success() {
        TransactionUpdateRequest request = TransactionUpdateRequest.builder()
                .amount(new BigDecimal("60000.00"))
                .description("Updated Salary")
                .build();

        when(authService.getCurrentUser()).thenReturn(testUser);
        when(transactionRepository.findById(10L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        TransactionResponse response = transactionService.updateTransaction(10L, request);

        assertNotNull(response);
        verify(transactionRepository, times(1)).save(transaction);
    }

    @Test
    void updateTransaction_ChangeDate_ThrowsBadRequestException() {
        TransactionUpdateRequest request = TransactionUpdateRequest.builder()
                .date(LocalDate.now().minusDays(5))
                .build();

        when(authService.getCurrentUser()).thenReturn(testUser);
        when(transactionRepository.findById(10L)).thenReturn(Optional.of(transaction));

        assertThrows(BadRequestException.class, () -> transactionService.updateTransaction(10L, request));
    }

    @Test
    void updateTransaction_NotFound_ThrowsResourceNotFoundException() {
        TransactionUpdateRequest request = TransactionUpdateRequest.builder().build();

        when(authService.getCurrentUser()).thenReturn(testUser);
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.updateTransaction(99L, request));
    }

    @Test
    void updateTransaction_OtherUsersTransaction_ThrowsForbiddenException() {
        User otherUser = User.builder().id(2L).username("other@example.com").build();
        Transaction otherUserTransaction = Transaction.builder()
                .id(10L)
                .user(otherUser)
                .amount(new BigDecimal("50000.00"))
                .date(LocalDate.now().minusDays(1))
                .category(category)
                .build();

        when(authService.getCurrentUser()).thenReturn(testUser);
        when(transactionRepository.findById(10L)).thenReturn(Optional.of(otherUserTransaction));

        assertThrows(ForbiddenException.class,
                () -> transactionService.updateTransaction(10L, TransactionUpdateRequest.builder().build()));
    }

    @Test
    void deleteTransaction_Success() {
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(transactionRepository.findById(10L)).thenReturn(Optional.of(transaction));

        MessageResponse response = transactionService.deleteTransaction(10L);

        assertNotNull(response);
        assertEquals("Transaction deleted successfully", response.getMessage());
        verify(transactionRepository, times(1)).delete(transaction);
    }

    @Test
    void deleteTransaction_NotFound_ThrowsResourceNotFoundException() {
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.deleteTransaction(99L));
    }

    @Test
    void deleteTransaction_OtherUsersTransaction_ThrowsForbiddenException() {
        User otherUser = User.builder().id(2L).username("other@example.com").build();
        Transaction otherUserTransaction = Transaction.builder()
                .id(10L)
                .user(otherUser)
                .amount(new BigDecimal("50000.00"))
                .date(LocalDate.now().minusDays(1))
                .category(category)
                .build();

        when(authService.getCurrentUser()).thenReturn(testUser);
        when(transactionRepository.findById(10L)).thenReturn(Optional.of(otherUserTransaction));

        assertThrows(ForbiddenException.class, () -> transactionService.deleteTransaction(10L));
    }
}
