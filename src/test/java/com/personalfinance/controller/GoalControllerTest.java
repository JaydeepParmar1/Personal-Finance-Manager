package com.personalfinance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalfinance.dto.*;
import com.personalfinance.service.GoalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GoalController.class)
@AutoConfigureMockMvc(addFilters = false)
class GoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GoalService goalService;

    @Test
    void createGoal_Success() throws Exception {
        GoalRequest request = GoalRequest.builder()
                .goalName("Emergency Fund")
                .targetAmount(new BigDecimal("5000.00"))
                .targetDate(LocalDate.now().plusMonths(6))
                .build();

        GoalResponse response = GoalResponse.builder()
                .id(1L)
                .goalName("Emergency Fund")
                .targetAmount(new BigDecimal("5000.00"))
                .targetDate(LocalDate.now().plusMonths(6))
                .currentProgress(new BigDecimal("1000.00"))
                .progressPercentage(20.0)
                .remainingAmount(new BigDecimal("4000.00"))
                .build();

        when(goalService.createGoal(any(GoalRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/goals")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.goalName").value("Emergency Fund"));
    }

    @Test
    void getAllGoals_Success() throws Exception {
        GoalResponse goal = GoalResponse.builder().id(1L).goalName("Emergency Fund").build();
        GoalListResponse response = GoalListResponse.builder().goals(List.of(goal)).build();

        when(goalService.getAllGoals()).thenReturn(response);

        mockMvc.perform(get("/api/goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goals[0].id").value(1));
    }

    @Test
    void getGoalById_Success() throws Exception {
        GoalResponse response = GoalResponse.builder().id(1L).goalName("Emergency Fund").build();

        when(goalService.getGoalById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/goals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateGoal_Success() throws Exception {
        GoalUpdateRequest request = GoalUpdateRequest.builder().targetAmount(new BigDecimal("6000.00")).build();
        GoalResponse response = GoalResponse.builder().id(1L).targetAmount(new BigDecimal("6000.00")).build();

        when(goalService.updateGoal(eq(1L), any(GoalUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/goals/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetAmount").value(6000.00));
    }

    @Test
    void updateGoal_PastTargetDate_BadRequest() throws Exception {
        GoalUpdateRequest request = GoalUpdateRequest.builder()
                .targetAmount(new BigDecimal("6000.00"))
                .targetDate(LocalDate.now().minusDays(1))
                .build();

        mockMvc.perform(put("/api/goals/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteGoal_Success() throws Exception {
        MessageResponse response = MessageResponse.builder().message("Goal deleted successfully").build();

        when(goalService.deleteGoal(1L)).thenReturn(response);

        mockMvc.perform(delete("/api/goals/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Goal deleted successfully"));
    }
}
