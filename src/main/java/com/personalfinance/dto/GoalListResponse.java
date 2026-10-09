package com.personalfinance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO wrapping a list of savings goals.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalListResponse {
    /** The savings goals with their progress information. */
    private List<GoalResponse> goals;
}
