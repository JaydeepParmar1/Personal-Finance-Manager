package com.personalfinance.service;

import com.personalfinance.dto.*;
import com.personalfinance.entity.Category;
import com.personalfinance.entity.CategoryType;
import com.personalfinance.entity.Goal;
import com.personalfinance.entity.Transaction;
import com.personalfinance.entity.User;
import com.personalfinance.exception.BadRequestException;
import com.personalfinance.exception.ForbiddenException;
import com.personalfinance.exception.ResourceNotFoundException;
import com.personalfinance.repository.GoalRepository;
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
class GoalServiceTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private GoalService goalService;

    private User testUser;
    private Goal testGoal;
    private Transaction incomeTransaction;
    private Transaction expenseTransaction;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).username("user@example.com").build();
        testGoal = Goal.builder()
                .id(1L)
                .user(testUser)
                .goalName("Emergency Fund")
                .targetAmount(new BigDecimal("5000.00"))
                .startDate(LocalDate.now().minusDays(10))
                .targetDate(LocalDate.now().plusMonths(6))
                .build();

        Category incomeCategory = Category.builder().name("Salary").type(CategoryType.INCOME).build();
        Category expenseCategory = Category.builder().name("Food").type(CategoryType.EXPENSE).build();

        incomeTransaction = Transaction.builder()
                .amount(new BigDecimal("3000.00"))
                .category(incomeCategory)
                .date(LocalDate.now().minusDays(5))
                .build();

        expenseTransaction = Transaction.builder()
                .amount(new BigDecimal("1000.00"))
                .category(expenseCategory)
                .date(LocalDate.now().minusDays(2))
                .build();
    }

    @Test
    void createGoal_Success() {
        GoalRequest request = GoalRequest.builder()
                .goalName("Emergency Fund")
                .targetAmount(new BigDecimal("5000.00"))
                .targetDate(LocalDate.now().plusMonths(6))
                .startDate(LocalDate.now().minusDays(10))
                .build();

        when(authService.getCurrentUser()).thenReturn(testUser);
        when(goalRepository.save(any(Goal.class))).thenReturn(testGoal);
        when(transactionRepository.findByUserAndDateGreaterThanEqual(eq(testUser), any(LocalDate.class)))
                .thenReturn(List.of(incomeTransaction, expenseTransaction));

        GoalResponse response = goalService.createGoal(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(new BigDecimal("2000.00"), response.getCurrentProgress()); // 3000 - 1000 = 2000
        assertEquals(new BigDecimal("3000.00"), response.getRemainingAmount()); // 5000 - 2000 = 3000
        assertEquals(40.0, response.getProgressPercentage()); // 2000 / 5000 * 100 = 40.0
    }

    @Test
    void createGoal_PastTargetDate_ThrowsBadRequestException() {
        GoalRequest request = GoalRequest.builder()
                .goalName("Invalid Goal")
                .targetAmount(new BigDecimal("5000.00"))
                .targetDate(LocalDate.now().minusDays(1))
                .build();

        when(authService.getCurrentUser()).thenReturn(testUser);

        assertThrows(BadRequestException.class, () -> goalService.createGoal(request));
    }

    @Test
    void getAllGoals_Success() {
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(goalRepository.findByUser(testUser)).thenReturn(List.of(testGoal));
        when(transactionRepository.findByUserAndDateGreaterThanEqual(eq(testUser), any(LocalDate.class)))
                .thenReturn(List.of());

        GoalListResponse response = goalService.getAllGoals();

        assertNotNull(response);
        assertEquals(1, response.getGoals().size());
        assertEquals("Emergency Fund", response.getGoals().get(0).getGoalName());
    }

    @Test
    void getGoalById_Success() {
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(goalRepository.findById(1L)).thenReturn(Optional.of(testGoal));
        when(transactionRepository.findByUserAndDateGreaterThanEqual(eq(testUser), any(LocalDate.class)))
                .thenReturn(List.of());

        GoalResponse response = goalService.getGoalById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    void getGoalById_NotFound_ThrowsResourceNotFoundException() {
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(goalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> goalService.getGoalById(99L));
    }

    @Test
    void getGoalById_OtherUsersGoal_ThrowsForbiddenException() {
        User otherUser = User.builder().id(2L).username("other@example.com").build();
        Goal otherUserGoal = Goal.builder()
                .id(1L)
                .user(otherUser)
                .goalName("Other Fund")
                .targetAmount(new BigDecimal("5000.00"))
                .startDate(LocalDate.now().minusDays(10))
                .targetDate(LocalDate.now().plusMonths(6))
                .build();

        when(authService.getCurrentUser()).thenReturn(testUser);
        when(goalRepository.findById(1L)).thenReturn(Optional.of(otherUserGoal));

        assertThrows(ForbiddenException.class, () -> goalService.getGoalById(1L));
    }

    @Test
    void updateGoal_Success() {
        GoalUpdateRequest request = GoalUpdateRequest.builder()
                .targetAmount(new BigDecimal("6000.00"))
                .targetDate(LocalDate.now().plusMonths(12))
                .build();

        when(authService.getCurrentUser()).thenReturn(testUser);
        when(goalRepository.findById(1L)).thenReturn(Optional.of(testGoal));
        when(goalRepository.save(any(Goal.class))).thenReturn(testGoal);

        GoalResponse response = goalService.updateGoal(1L, request);

        assertNotNull(response);
        verify(goalRepository, times(1)).save(testGoal);
    }

    @Test
    void updateGoal_OtherUsersGoal_ThrowsForbiddenException() {
        User otherUser = User.builder().id(2L).username("other@example.com").build();
        Goal otherUserGoal = Goal.builder()
                .id(1L)
                .user(otherUser)
                .goalName("Other Fund")
                .targetAmount(new BigDecimal("5000.00"))
                .startDate(LocalDate.now().minusDays(10))
                .targetDate(LocalDate.now().plusMonths(6))
                .build();

        when(authService.getCurrentUser()).thenReturn(testUser);
        when(goalRepository.findById(1L)).thenReturn(Optional.of(otherUserGoal));

        assertThrows(ForbiddenException.class,
                () -> goalService.updateGoal(1L, GoalUpdateRequest.builder().build()));
    }

    @Test
    void deleteGoal_Success() {
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(goalRepository.findById(1L)).thenReturn(Optional.of(testGoal));

        MessageResponse response = goalService.deleteGoal(1L);

        assertNotNull(response);
        assertEquals("Goal deleted successfully", response.getMessage());
        verify(goalRepository, times(1)).delete(testGoal);
    }

    @Test
    void deleteGoal_OtherUsersGoal_ThrowsForbiddenException() {
        User otherUser = User.builder().id(2L).username("other@example.com").build();
        Goal otherUserGoal = Goal.builder()
                .id(1L)
                .user(otherUser)
                .goalName("Other Fund")
                .targetAmount(new BigDecimal("5000.00"))
                .startDate(LocalDate.now().minusDays(10))
                .targetDate(LocalDate.now().plusMonths(6))
                .build();

        when(authService.getCurrentUser()).thenReturn(testUser);
        when(goalRepository.findById(1L)).thenReturn(Optional.of(otherUserGoal));

        assertThrows(ForbiddenException.class, () -> goalService.deleteGoal(1L));
    }
}
