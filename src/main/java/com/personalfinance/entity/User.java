package com.personalfinance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JPA entity representing a registered user.
 *
 * <p>All personal financial data (transactions, custom categories, goals) is
 * owned by a user, enforcing complete data isolation between accounts.</p>
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /** Unique identifier of the user. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Username; must be a unique email address. */
    @Column(nullable = false, unique = true)
    private String username;

    /** BCrypt-hashed password. */
    @Column(nullable = false)
    private String password;

    /** Full name of the user. */
    @Column(nullable = false)
    private String fullName;

    /** Contact phone number of the user. */
    @Column(nullable = false)
    private String phoneNumber;

    /** Timestamp when the account was created. */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Sets the creation timestamp before the entity is persisted. */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
