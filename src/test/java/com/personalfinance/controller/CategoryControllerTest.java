package com.personalfinance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalfinance.dto.CategoryListResponse;
import com.personalfinance.dto.CategoryRequest;
import com.personalfinance.dto.CategoryResponse;
import com.personalfinance.dto.MessageResponse;
import com.personalfinance.entity.CategoryType;
import com.personalfinance.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @Test
    void getAllCategories_Success() throws Exception {
        CategoryResponse cat1 = CategoryResponse.builder().name("Salary").type(CategoryType.INCOME).isCustom(false).build();
        CategoryListResponse response = CategoryListResponse.builder().categories(List.of(cat1)).build();

        when(categoryService.getAllCategories()).thenReturn(response);

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories[0].name").value("Salary"))
                .andExpect(jsonPath("$.categories[0].isCustom").value(false));
    }

    @Test
    void createCustomCategory_Success() throws Exception {
        CategoryRequest request = CategoryRequest.builder().name("Bonus").type(CategoryType.INCOME).build();
        CategoryResponse response = CategoryResponse.builder().name("Bonus").type(CategoryType.INCOME).isCustom(true).build();

        when(categoryService.createCustomCategory(any(CategoryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Bonus"))
                .andExpect(jsonPath("$.isCustom").value(true))
                .andExpect(jsonPath("$.custom").value(true));
    }

    @Test
    void deleteCustomCategory_Success() throws Exception {
        MessageResponse response = MessageResponse.builder().message("Category deleted successfully").build();

        when(categoryService.deleteCustomCategory("Bonus")).thenReturn(response);

        mockMvc.perform(delete("/api/categories/Bonus").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Category deleted successfully"));
    }
}
