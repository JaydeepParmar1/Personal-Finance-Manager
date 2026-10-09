package com.personalfinance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO wrapping a list of transactions.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionListResponse {
    /** The transactions, sorted by newest first. */
    private List<TransactionResponse> transactions;
}
