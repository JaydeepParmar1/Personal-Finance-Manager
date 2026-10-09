package com.personalfinance.controller;

import com.personalfinance.dto.CategoryListResponse;
import com.personalfinance.dto.CategoryRequest;
import com.personalfinance.dto.CategoryResponse;
import com.personalfinance.dto.MessageResponse;
import com.personalfinance.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<CategoryListResponse> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCustomCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createCustomCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<MessageResponse> deleteCustomCategory(@PathVariable String name) {
        MessageResponse response = categoryService.deleteCustomCategory(name);
        return ResponseEntity.ok(response);
    }
}
