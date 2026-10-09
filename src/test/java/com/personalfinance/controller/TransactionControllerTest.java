package com.personalfinance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalfinance.dto.*;
import com.personalfinance.entity.CategoryType;
import com.personalfinance.service.TransactionService;
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

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @Test
    void createTransaction_Success() throws Exception {
        TransactionRequest request = TransactionRequest.builder()
                .amount(new BigDecimal("50000.00"))
                .date(LocalDate.of(2024, 1, 15))
                .category("Salary")
                .description("January Salary")
                .build();

        TransactionResponse response = TransactionResponse.builder()
                .id(1L)
                .amount(new BigDecimal("50000.00"))
                .date(LocalDate.of(2024, 1, 15))
                .category("Salary")
                .description("January Salary")
                .type(CategoryType.INCOME)
                .build();

        when(transactionService.createTransaction(any(TransactionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/transactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.category").value("Salary"));
    }

    @Test
    void getTransactions_Success() throws Exception {
        TransactionResponse tx = TransactionResponse.builder()
                .id(1L)
                .amount(new BigDecimal("50000.00"))
                .date(LocalDate.of(2024, 1, 15))
                .category("Salary")
                .type(CategoryType.INCOME)
                .build();

        TransactionListResponse response = TransactionListResponse.builder().transactions(List.of(tx)).build();

        when(transactionService.getTransactions(any(), any(), any(), any(), any())).thenReturn(response);

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions[0].id").value(1));
    }

    @Test
    void updateTransaction_Success() throws Exception {
        TransactionUpdateRequest request = TransactionUpdateRequest.builder()
                .amount(new BigDecimal("60000.00"))
                .build();

        TransactionResponse response = TransactionResponse.builder()
                .id(1L)
                .amount(new BigDecimal("60000.00"))
                .build();

        when(transactionService.updateTransaction(eq(1L), any(TransactionUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/transactions/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(60000.00));
    }

    @Test
    void updateTransaction_NegativeAmount_BadRequest() throws Exception {
        TransactionUpdateRequest request = TransactionUpdateRequest.builder()
                .amount(new BigDecimal("-100.00"))
                .build();

        mockMvc.perform(put("/api/transactions/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteTransaction_Success() throws Exception {
        MessageResponse response = MessageResponse.builder().message("Transaction deleted successfully").build();

        when(transactionService.deleteTransaction(1L)).thenReturn(response);

        mockMvc.perform(delete("/api/transactions/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transaction deleted successfully"));
    }
}
