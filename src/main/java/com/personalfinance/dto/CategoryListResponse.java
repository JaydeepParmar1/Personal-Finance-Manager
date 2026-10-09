package com.personalfinance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO wrapping a list of categories.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryListResponse {
    /** The categories accessible to the user (defaults plus own custom categories). */
    private List<CategoryResponse> categories;
}
