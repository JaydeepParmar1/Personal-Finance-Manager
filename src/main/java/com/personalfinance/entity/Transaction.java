package com.personalfinance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * JPA entity representing a financial transaction (income or expense).
 */
@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    /** Unique identifier of the transaction. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** User who owns the transaction. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Transaction amount; always positive. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /** Transaction date in YYYY-MM-DD format. */
    @Column(nullable = false)
    private LocalDate date;

    /** Category the transaction belongs to. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /** Optional description of the transaction. */
    @Column(length = 500)
    private String description;
}
