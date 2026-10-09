package com.personalfinance.repository;

import com.personalfinance.entity.Category;
import com.personalfinance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Category} entities.
 *
 * <p>Default categories are shared between all users ({@code user} is
 * {@code null}), while custom categories are owned by a single user. Name
 * lookups are case-insensitive.</p>
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Finds all default categories (shared, not user-owned).
     *
     * @return the default categories
     */
    List<Category> findByIsCustomFalse();

    /**
     * Finds all custom categories owned by the given user.
     *
     * @param user the owning user
     * @return the user's custom categories
     */
    List<Category> findByUser(User user);

    /**
     * Finds a default category or a custom category owned by the given user,
     * matching the name case-insensitively.
     *
     * @param name the category name
     * @param user the user who may own a custom category with this name
     * @return the matching category, or {@link Optional#empty()} if none is accessible
     */
    @Query("SELECT c FROM Category c WHERE LOWER(c.name) = LOWER(:name) AND (c.isCustom = false OR c.user = :user)")
    Optional<Category> findByNameIgnoreCaseAndUserOrDefault(@Param("name") String name, @Param("user") User user);

    /**
     * Checks whether a default category or a custom category owned by the given
     * user exists with the given name (case-insensitive).
     *
     * @param name the category name
     * @param user the user who may own a custom category with this name
     * @return {@code true} if such a category exists
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Category c WHERE LOWER(c.name) = LOWER(:name) AND (c.isCustom = false OR c.user = :user)")
    boolean existsByNameForUserOrDefault(@Param("name") String name, @Param("user") User user);

    /**
     * Checks whether any custom category exists with the given name, regardless
     * of its owner (case-insensitive). Used to distinguish another user's
     * category (HTTP 403) from a non-existent category (HTTP 404).
     *
     * @param name the category name
     * @return {@code true} if any custom category with this name exists
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Category c WHERE LOWER(c.name) = LOWER(:name) AND c.isCustom = true")
    boolean existsCustomByName(@Param("name") String name);
}
