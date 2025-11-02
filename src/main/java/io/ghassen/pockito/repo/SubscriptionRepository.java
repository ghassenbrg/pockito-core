package io.ghassen.pockito.repo;

import io.ghassen.pockito.domain.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Subscription entity operations.
 * 
 * Provides standard CRUD operations and custom query methods for subscription management.
 * Extends JpaRepository to inherit common database operations.
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, String> {

    /**
     * Find all subscriptions belonging to a specific user.
     * 
     * @param username the username of the user
     * @return list of subscriptions owned by the user
     */
    List<Subscription> findByUserUsernameOrderByNameAsc(String username);

    /**
     * Find all active subscriptions for a specific user.
     * 
     * @param username the username of the user
     * @return list of active subscriptions owned by the user
     */
    List<Subscription> findByUserUsernameAndIsActiveTrueOrderByNameAsc(String username);

    /**
     * Find subscription by user and ID.
     * 
     * @param username the username of the user
     * @param subscriptionId the subscription ID
     * @return optional containing the subscription if it exists and is owned by the user
     */
    Optional<Subscription> findByUserUsernameAndId(String username, String subscriptionId);

    /**
     * Find all subscriptions with next due date on or before the specified date.
     * 
     * @param date the date to check against
     * @return list of subscriptions with next due date on or before the date
     */
    List<Subscription> findByNextDueDateLessThanEqual(LocalDate date);

    /**
     * Find all active subscriptions with next due date on or before the specified date.
     * 
     * @param date the date to check against
     * @return list of active subscriptions with next due date on or before the date
     */
    List<Subscription> findByIsActiveTrueAndNextDueDateLessThanEqual(LocalDate date);

    /**
     * Count subscriptions belonging to a specific user.
     * 
     * @param username the username of the user
     * @return the number of subscriptions owned by the user
     */
    long countByUserUsername(String username);

    /**
     * Count active subscriptions belonging to a specific user.
     * 
     * @param username the username of the user
     * @return the number of active subscriptions owned by the user
     */
    long countByUserUsernameAndIsActiveTrue(String username);
}

