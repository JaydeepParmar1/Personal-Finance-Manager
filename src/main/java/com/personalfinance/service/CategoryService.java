package com.personalfinance.service;

import com.personalfinance.dto.CategoryListResponse;
import com.personalfinance.dto.CategoryRequest;
import com.personalfinance.dto.CategoryResponse;
import com.personalfinance.dto.MessageResponse;
import com.personalfinance.entity.Category;
import com.personalfinance.entity.User;
import com.personalfinance.exception.BadRequestException;
import com.personalfinance.exception.ConflictException;
import com.personalfinance.exception.ForbiddenException;
import com.personalfinance.exception.ResourceNotFoundException;
import com.personalfinance.repository.CategoryRepository;
import com.personalfinance.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service layer for managing transaction categories.
 *
 * <p>The system provides predefined default categories that cannot be modified
 * or deleted. Users can create their own custom categories; custom names must be
 * unique per user. Categories referenced by transactions cannot be deleted.
 * Default categories and other users' custom categories are protected from
 * modification or deletion.</p>
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final AuthService authService;

    /**
     * Retrieves all categories accessible to the currently authenticated user:
     * the shared default categories plus the user's own custom categories.
     *
     * @return a list of category response DTOs
     */
    @Transactional(readOnly = true)
    public CategoryListResponse getAllCategories() {
        User currentUser = authService.getCurrentUser();
        List<Category> defaultCategories = categoryRepository.findByIsCustomFalse();
        List<Category> userCustomCategories = categoryRepository.findByUser(currentUser);

        List<CategoryResponse> combined = new ArrayList<>();

        for (Category cat : defaultCategories) {
            combined.add(CategoryResponse.builder()
                    .name(cat.getName())
                    .type(cat.getType())
                    .isCustom(false)
                    .build());
        }

        for (Category cat : userCustomCategories) {
            combined.add(CategoryResponse.builder()
                    .name(cat.getName())
                    .type(cat.getType())
                    .isCustom(true)
                    .build());
        }

        return CategoryListResponse.builder()
                .categories(combined)
                .build();
    }

    /**
     * Creates a new custom category for the currently authenticated user.
     *
     * @param request the category details (name and type)
     * @return the created category response DTO
     * @throws ConflictException if a default category or one of the user's custom
     *                           categories already uses the same name
     */
    @Transactional
    public CategoryResponse createCustomCategory(CategoryRequest request) {
        User currentUser = authService.getCurrentUser();
        String trimmedName = request.getName().trim();

        if (categoryRepository.existsByNameForUserOrDefault(trimmedName, currentUser)) {
            throw new ConflictException("Category with name '" + trimmedName + "' already exists");
        }

        Category category = Category.builder()
                .name(trimmedName)
                .type(request.getType())
                .isCustom(true)
                .user(currentUser)
                .build();

        Category saved = categoryRepository.save(category);

        return CategoryResponse.builder()
                .name(saved.getName())
                .type(saved.getType())
                .isCustom(true)
                .build();
    }

    /**
     * Deletes a custom category of the currently authenticated user.
     *
     * @param name the name of the category to delete
     * @return a confirmation message
     * @throws ResourceNotFoundException if no default or own custom category with the name exists
     * @throws ForbiddenException        if the category belongs to another user
     * @throws BadRequestException       if the category is a default category or is
     *                                   referenced by transactions
     */
    @Transactional
    public MessageResponse deleteCustomCategory(String name) {
        User currentUser = authService.getCurrentUser();
        String trimmedName = name.trim();

        // Resolve the category among shared defaults and the current user's own custom categories
        Category category = categoryRepository.findByNameIgnoreCaseAndUserOrDefault(trimmedName, currentUser)
                .orElse(null);

        if (category == null) {
            // Not visible to this user: either another user's custom category or a non-existent name
            if (categoryRepository.existsCustomByName(trimmedName)) {
                throw new ForbiddenException("Access denied: category belongs to another user");
            }
            throw new ResourceNotFoundException("Category '" + trimmedName + "' not found");
        }

        // Default categories cannot be deleted
        if (Boolean.FALSE.equals(category.getIsCustom())) {
            throw new BadRequestException("Default categories cannot be deleted");
        }

        // Check if category is referenced in transactions
        if (transactionRepository.existsByCategoryAndUser(category, currentUser)) {
            throw new BadRequestException("Category currently referenced by transactions cannot be deleted");
        }

        categoryRepository.delete(category);

        return MessageResponse.builder()
                .message("Category deleted successfully")
                .build();
    }

    /**
     * Resolves a category by name for the given user, accepting default categories
     * and the user's own custom categories (case-insensitive).
     *
     * @param name the name of the category
     * @param user the user who references the category
     * @return the resolved category
     * @throws BadRequestException if no accessible category with the name exists
     */
    public Category getCategoryByNameForUser(String name, User user) {
        return categoryRepository.findByNameIgnoreCaseAndUserOrDefault(name.trim(), user)
                .orElseThrow(() -> new BadRequestException("Invalid category: " + name));
    }
}
