package com.personalfinance.dto;

import com.personalfinance.entity.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a custom category.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequest {

    /** Name of the category; must be unique per user. */
    @NotBlank(message = "Category name is required")
    private String name;

    /** Type of the category (INCOME or EXPENSE). */
    @NotNull(message = "Category type (INCOME/EXPENSE) is required")
    private CategoryType type;
}
