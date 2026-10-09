package com.personalfinance.service;

import com.personalfinance.dto.CategoryListResponse;
import com.personalfinance.dto.CategoryRequest;
import com.personalfinance.dto.CategoryResponse;
import com.personalfinance.dto.MessageResponse;
import com.personalfinance.entity.Category;
import com.personalfinance.entity.CategoryType;
import com.personalfinance.entity.User;
import com.personalfinance.exception.BadRequestException;
import com.personalfinance.exception.ConflictException;
import com.personalfinance.exception.ForbiddenException;
import com.personalfinance.exception.ResourceNotFoundException;
import com.personalfinance.repository.CategoryRepository;
import com.personalfinance.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private CategoryService categoryService;

    private User testUser;
    private Category defaultCategory;
    private Category customCategory;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("user@example.com")
                .build();

        defaultCategory = Category.builder()
                .id(1L)
                .name("Salary")
                .type(CategoryType.INCOME)
                .isCustom(false)
                .user(null)
                .build();

        customCategory = Category.builder()
                .id(2L)
                .name("SideGig")
                .type(CategoryType.INCOME)
                .isCustom(true)
                .user(testUser)
                .build();
    }

    @Test
    void getAllCategories_Success() {
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(categoryRepository.findByIsCustomFalse()).thenReturn(List.of(defaultCategory));
        when(categoryRepository.findByUser(testUser)).thenReturn(List.of(customCategory));

        CategoryListResponse response = categoryService.getAllCategories();

        assertNotNull(response);
        assertEquals(2, response.getCategories().size());
        assertFalse(response.getCategories().get(0).getIsCustom());
        assertTrue(response.getCategories().get(1).getIsCustom());
    }

    @Test
    void createCustomCategory_Success() {
        CategoryRequest request = CategoryRequest.builder()
                .name("SideGig")
                .type(CategoryType.INCOME)
                .build();

        when(authService.getCurrentUser()).thenReturn(testUser);
        when(categoryRepository.existsByNameForUserOrDefault("SideGig", testUser)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(customCategory);

        CategoryResponse response = categoryService.createCustomCategory(request);

        assertNotNull(response);
        assertEquals("SideGig", response.getName());
        assertTrue(response.getIsCustom());
    }

    @Test
    void createCustomCategory_AlreadyExists_ThrowsConflictException() {
        CategoryRequest request = CategoryRequest.builder()
                .name("SideGig")
                .type(CategoryType.INCOME)
                .build();

        when(authService.getCurrentUser()).thenReturn(testUser);
        when(categoryRepository.existsByNameForUserOrDefault("SideGig", testUser)).thenReturn(true);

        assertThrows(ConflictException.class, () -> categoryService.createCustomCategory(request));
    }

    @Test
    void deleteCustomCategory_Success() {
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(categoryRepository.findByNameIgnoreCaseAndUserOrDefault("SideGig", testUser)).thenReturn(Optional.of(customCategory));
        when(transactionRepository.existsByCategoryAndUser(customCategory, testUser)).thenReturn(false);

        MessageResponse response = categoryService.deleteCustomCategory("SideGig");

        assertNotNull(response);
        assertEquals("Category deleted successfully", response.getMessage());
        verify(categoryRepository, times(1)).delete(customCategory);
    }

    @Test
    void deleteCustomCategory_DefaultCategory_ThrowsBadRequestException() {
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(categoryRepository.findByNameIgnoreCaseAndUserOrDefault("Salary", testUser)).thenReturn(Optional.of(defaultCategory));

        assertThrows(BadRequestException.class, () -> categoryService.deleteCustomCategory("Salary"));
    }

    @Test
    void deleteCustomCategory_NotFound_ThrowsResourceNotFoundException() {
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(categoryRepository.findByNameIgnoreCaseAndUserOrDefault("Unknown", testUser)).thenReturn(Optional.empty());
        when(categoryRepository.existsCustomByName("Unknown")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteCustomCategory("Unknown"));
    }

    @Test
    void deleteCustomCategory_OtherUsersCategory_ThrowsForbiddenException() {
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(categoryRepository.findByNameIgnoreCaseAndUserOrDefault("SideGig", testUser)).thenReturn(Optional.empty());
        when(categoryRepository.existsCustomByName("SideGig")).thenReturn(true);

        assertThrows(ForbiddenException.class, () -> categoryService.deleteCustomCategory("SideGig"));
    }

    @Test
    void deleteCustomCategory_ReferencedInTransactions_ThrowsBadRequestException() {
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(categoryRepository.findByNameIgnoreCaseAndUserOrDefault("SideGig", testUser)).thenReturn(Optional.of(customCategory));
        when(transactionRepository.existsByCategoryAndUser(customCategory, testUser)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> categoryService.deleteCustomCategory("SideGig"));
    }

    @Test
    void getCategoryByNameForUser_Success() {
        when(categoryRepository.findByNameIgnoreCaseAndUserOrDefault("Salary", testUser))
                .thenReturn(Optional.of(defaultCategory));

        Category category = categoryService.getCategoryByNameForUser("Salary", testUser);

        assertNotNull(category);
        assertEquals("Salary", category.getName());
    }

    @Test
    void getCategoryByNameForUser_Invalid_ThrowsBadRequestException() {
        when(categoryRepository.findByNameIgnoreCaseAndUserOrDefault("Invalid", testUser))
                .thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> categoryService.getCategoryByNameForUser("Invalid", testUser));
    }
}
