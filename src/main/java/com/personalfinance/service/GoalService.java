package com.personalfinance.service;

import com.personalfinance.dto.*;
import com.personalfinance.entity.CategoryType;
import com.personalfinance.entity.Goal;
import com.personalfinance.entity.Transaction;
import com.personalfinance.entity.User;
import com.personalfinance.exception.BadRequestException;
import com.personalfinance.exception.ForbiddenException;
import com.personalfinance.exception.ResourceNotFoundException;
import com.personalfinance.repository.GoalRepository;
import com.personalfinance.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for managing savings goals.
 *
 * <p>Goal progress is calculated independently per goal as
 * (total income - total expenses) since the goal start date. Accessing another
 * user's goal results in a {@link ForbiddenException} (HTTP 403).</p>
 */
@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final TransactionRepository transactionRepository;
    private final AuthService authService;

    /**
     * Creates a new savings goal for the currently authenticated user.
     *
     * @param request the goal details (goal name, target amount, target date, optional start date)
     * @return the created goal including progress information
     * @throws BadRequestException if the target date is not in the future
     */
    @Transactional
    public GoalResponse createGoal(GoalRequest request) {
        User currentUser = authService.getCurrentUser();

        if (!request.getTargetDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Target date must be a future date");
        }

        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now();

        Goal goal = Goal.builder()
                .user(currentUser)
                .goalName(request.getGoalName())
                .targetAmount(request.getTargetAmount())
                .targetDate(request.getTargetDate())
                .startDate(startDate)
                .build();

        Goal saved = goalRepository.save(goal);
        return buildGoalResponse(saved, currentUser);
    }

    /**
     * Retrieves all savings goals of the currently authenticated user with progress.
     *
     * @return a list of goal response DTOs
     */
    @Transactional(readOnly = true)
    public GoalListResponse getAllGoals() {
        User currentUser = authService.getCurrentUser();
        List<Goal> goals = goalRepository.findByUser(currentUser);

        List<GoalResponse> responses = goals.stream()
                .map(goal -> buildGoalResponse(goal, currentUser))
                .collect(Collectors.toList());

        return GoalListResponse.builder()
                .goals(responses)
                .build();
    }

    /**
     * Retrieves a single savings goal of the currently authenticated user by its id.
     *
     * @param id the id of the goal
     * @return the goal with progress information
     * @throws ResourceNotFoundException if no goal with the given id exists
     * @throws ForbiddenException        if the goal belongs to another user
     */
    @Transactional(readOnly = true)
    public GoalResponse getGoalById(Long id) {
        User currentUser = authService.getCurrentUser();
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + id));

        if (!goal.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Access denied: goal belongs to another user");
        }

        return buildGoalResponse(goal, currentUser);
    }

    /**
     * Updates an existing savings goal of the currently authenticated user
     * (goal name, target amount and/or target date) and recalculates its progress.
     *
     * @param id      the id of the goal to update
     * @param request the fields to update
     * @return the updated goal with progress information
     * @throws ResourceNotFoundException if no goal with the given id exists
     * @throws ForbiddenException        if the goal belongs to another user
     * @throws BadRequestException       if the target date is not in the future
     */
    @Transactional
    public GoalResponse updateGoal(Long id, GoalUpdateRequest request) {
        User currentUser = authService.getCurrentUser();
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + id));

        if (!goal.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Access denied: goal belongs to another user");
        }

        if (request.getTargetDate() != null) {
            if (!request.getTargetDate().isAfter(LocalDate.now())) {
                throw new BadRequestException("Target date must be a future date");
            }
            goal.setTargetDate(request.getTargetDate());
        }

        if (request.getTargetAmount() != null) {
            goal.setTargetAmount(request.getTargetAmount());
        }

        if (request.getGoalName() != null && !request.getGoalName().isBlank()) {
            goal.setGoalName(request.getGoalName());
        }

        Goal updated = goalRepository.save(goal);
        return buildGoalResponse(updated, currentUser);
    }

    /**
     * Deletes a savings goal of the currently authenticated user.
     *
     * @param id the id of the goal to delete
     * @return a confirmation message
     * @throws ResourceNotFoundException if no goal with the given id exists
     * @throws ForbiddenException        if the goal belongs to another user
     */
    @Transactional
    public MessageResponse deleteGoal(Long id) {
        User currentUser = authService.getCurrentUser();
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + id));

        if (!goal.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Access denied: goal belongs to another user");
        }

        goalRepository.delete(goal);

        return MessageResponse.builder()
                .message("Goal deleted successfully")
                .build();
    }

    /**
     * Builds a goal response DTO with the current progress, progress percentage
     * and remaining amount calculated from the user's transactions since the
     * goal start date.
     *
     * @param goal the goal entity
     * @param user the owner of the goal
     * @return the goal response DTO with computed progress values
     */
    private GoalResponse buildGoalResponse(Goal goal, User user) {
        List<Transaction> transactions = transactionRepository.findByUserAndDateGreaterThanEqual(user, goal.getStartDate());

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            if (t.getCategory().getType() == CategoryType.INCOME) {
                totalIncome = totalIncome.add(t.getAmount());
            } else if (t.getCategory().getType() == CategoryType.EXPENSE) {
                totalExpenses = totalExpenses.add(t.getAmount());
            }
        }

        BigDecimal currentProgress = totalIncome.subtract(totalExpenses);

        BigDecimal targetAmount = goal.getTargetAmount();
        BigDecimal remainingAmount = targetAmount.subtract(currentProgress);
        if (remainingAmount.compareTo(BigDecimal.ZERO) < 0) {
            remainingAmount = BigDecimal.ZERO;
        }

        double progressPercentage = 0.0;
        if (targetAmount.compareTo(BigDecimal.ZERO) > 0) {
            progressPercentage = currentProgress
                    .multiply(BigDecimal.valueOf(100))
                    .divide(targetAmount, 2, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        return GoalResponse.builder()
                .id(goal.getId())
                .goalName(goal.getGoalName())
                .targetAmount(goal.getTargetAmount())
                .targetDate(goal.getTargetDate())
                .startDate(goal.getStartDate())
                .currentProgress(currentProgress)
                .progressPercentage(progressPercentage)
                .remainingAmount(remainingAmount)
                .build();
    }
}
