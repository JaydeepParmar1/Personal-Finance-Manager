package com.personalfinance.repository;

import com.personalfinance.entity.Goal;
import com.personalfinance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link Goal} entities.
 *
 * <p>All custom queries are scoped by the owning user to enforce data isolation.</p>
 */
@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {

    /**
     * Finds all savings goals of a user.
     *
     * @param user the owning user
     * @return the user's savings goals
     */
    List<Goal> findByUser(User user);
}
