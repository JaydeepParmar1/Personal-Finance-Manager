package com.personalfinance.config;

import com.personalfinance.entity.Category;
import com.personalfinance.entity.CategoryType;
import com.personalfinance.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds the database with the predefined default categories on startup.
 *
 * <p>Default categories are shared between all users and cannot be modified
 * or deleted: Salary (INCOME) and Food, Rent, Transportation, Entertainment,
 * Healthcare, Utilities (EXPENSE).</p>
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    /**
     * Inserts the default categories if the database is empty.
     *
     * @param args the command line arguments
     * @throws Exception if seeding fails
     */
    @Override
    public void run(String... args) throws Exception {
        if (categoryRepository.findByIsCustomFalse().isEmpty()) {
            List<Category> defaultCategories = List.of(
                Category.builder().name("Salary").type(CategoryType.INCOME).isCustom(false).user(null).build(),
                Category.builder().name("Food").type(CategoryType.EXPENSE).isCustom(false).user(null).build(),
                Category.builder().name("Rent").type(CategoryType.EXPENSE).isCustom(false).user(null).build(),
                Category.builder().name("Transportation").type(CategoryType.EXPENSE).isCustom(false).user(null).build(),
                Category.builder().name("Entertainment").type(CategoryType.EXPENSE).isCustom(false).user(null).build(),
                Category.builder().name("Healthcare").type(CategoryType.EXPENSE).isCustom(false).user(null).build(),
                Category.builder().name("Utilities").type(CategoryType.EXPENSE).isCustom(false).user(null).build()
            );
            categoryRepository.saveAll(defaultCategories);
        }
    }
}
