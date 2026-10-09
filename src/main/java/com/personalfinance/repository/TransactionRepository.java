package com.personalfinance.repository;

import com.personalfinance.entity.Category;
import com.personalfinance.entity.CategoryType;
import com.personalfinance.entity.Transaction;
import com.personalfinance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for {@link Transaction} entities.
 *
 * <p>All custom queries are scoped by the owning user to enforce data isolation.</p>
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Finds all transactions of a user, ordered by date (newest first).
     *
     * @param user the owning user
     * @return the user's transactions
     */
    List<Transaction> findByUserOrderByDateDescIdDesc(User user);

    /**
     * Checks whether any transaction of the given user references the category.
     *
     * @param category the category to check
     * @param user     the owning user
     * @return {@code true} if at least one transaction references the category
     */
    boolean existsByCategoryAndUser(Category category, User user);

    /**
     * Finds transactions of a user matching the optional filters, ordered by
     * date (newest first) and then by id (newest first).
     *
     * @param user         the owning user
     * @param startDate    the start of the date range (inclusive), may be {@code null}
     * @param endDate      the end of the date range (inclusive), may be {@code null}
     * @param categoryId   the category id to filter by, may be {@code null}
     * @param categoryName the category name to filter by, may be {@code null}
     * @param type         the transaction type to filter by, may be {@code null}
     * @return the matching transactions
     */
    @Query("SELECT t FROM Transaction t WHERE t.user = :user " +
           "AND (:startDate IS NULL OR t.date >= :startDate) " +
           "AND (:endDate IS NULL OR t.date <= :endDate) " +
           "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
           "AND (:categoryName IS NULL OR t.category.name = :categoryName) " +
           "AND (:type IS NULL OR t.category.type = :type) " +
           "ORDER BY t.date DESC, t.id DESC")
    List<Transaction> findFilteredTransactions(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("categoryId") Long categoryId,
            @Param("categoryName") String categoryName,
            @Param("type") CategoryType type
    );

    /**
     * Finds all transactions of a user on or after the given date.
     *
     * @param user      the owning user
     * @param startDate the start date (inclusive)
     * @return the matching transactions
     */
    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND t.date >= :startDate")
    List<Transaction> findByUserAndDateGreaterThanEqual(@Param("user") User user, @Param("startDate") LocalDate startDate);

    /**
     * Finds all transactions of a user within the given date range (inclusive).
     *
     * @param user      the owning user
     * @param startDate the start date (inclusive)
     * @param endDate   the end date (inclusive)
     * @return the matching transactions
     */
    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND t.date >= :startDate AND t.date <= :endDate")
    List<Transaction> findByUserAndDateBetween(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
