package com.personalfinance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.personalfinance.entity.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO representing a transaction category.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {
    /** Name of the category. */
    private String name;
    /** Type of the category (INCOME or EXPENSE). */
    private CategoryType type;

    /** Whether the category is a user-defined custom category. */
    @JsonProperty("isCustom")
    private Boolean isCustom;
}
