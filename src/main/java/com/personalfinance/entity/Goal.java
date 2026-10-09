package com.personalfinance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * JPA entity representing a savings goal.
 */
@Entity
@Table(name = "goals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Goal {

    /** Unique identifier of the goal. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** User who owns the goal. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Descriptive name of the goal. */
    @Column(nullable = false)
    private String goalName;

    /** Target amount to be saved. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal targetAmount;

    /** Date by which the goal should be reached; must be in the future. */
    @Column(nullable = false)
    private LocalDate targetDate;

    /** Date from which progress is calculated. */
    @Column(nullable = false)
    private LocalDate startDate;
}
