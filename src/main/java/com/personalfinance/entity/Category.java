package com.personalfinance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity representing a transaction category.
 *
 * <p>Default categories are shared between all users ({@code user} is
 * {@code null} and {@code isCustom} is {@code false}); custom categories are
 * owned by exactly one user.</p>
 */
@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    /** Unique identifier of the category. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Name of the category. */
    @Column(nullable = false)
    private String name;

    /** Type of the category (INCOME or EXPENSE). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryType type;

    /** Whether the category is a user-defined custom category. */
    @Column(nullable = false)
    private Boolean isCustom;

    /** Owner of the category; {@code null} for shared default categories. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;
}
