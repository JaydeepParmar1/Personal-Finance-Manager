package com.personalfinance.controller;

import com.personalfinance.dto.*;
import com.personalfinance.service.GoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing savings goals.
 *
 * <p>All endpoints require authentication. Goal progress is calculated from the
 * authenticated user's transactions only, ensuring complete data isolation.</p>
 */
@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    /**
     * Creates a new savings goal for the authenticated user.
     *
     * @param request the goal details (goal name, target amount, target date, optional start date)
     * @return the created goal including progress information with HTTP 201
     */
    @PostMapping
    public ResponseEntity<GoalResponse> createGoal(@Valid @RequestBody GoalRequest request) {
        GoalResponse response = goalService.createGoal(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all savings goals of the authenticated user with their current progress.
     *
     * @return the list of goals with HTTP 200
     */
    @GetMapping
    public ResponseEntity<GoalListResponse> getAllGoals() {
        GoalListResponse response = goalService.getAllGoals();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a single savings goal of the authenticated user by its id.
     *
     * @param id the id of the goal
     * @return the goal with progress information with HTTP 200
     */
    @GetMapping("/{id}")
    public ResponseEntity<GoalResponse> getGoalById(@PathVariable Long id) {
        GoalResponse response = goalService.getGoalById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates an existing savings goal of the authenticated user
     * (goal name, target amount and/or target date).
     *
     * @param id      the id of the goal to update
     * @param request the fields to update
     * @return the updated goal with recalculated progress with HTTP 200
     */
    @PutMapping("/{id}")
    public ResponseEntity<GoalResponse> updateGoal(
            @PathVariable Long id,
            @Valid @RequestBody GoalUpdateRequest request) {
        GoalResponse response = goalService.updateGoal(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a savings goal of the authenticated user.
     *
     * @param id the id of the goal to delete
     * @return a confirmation message with HTTP 200
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteGoal(@PathVariable Long id) {
        MessageResponse response = goalService.deleteGoal(id);
        return ResponseEntity.ok(response);
    }
}
